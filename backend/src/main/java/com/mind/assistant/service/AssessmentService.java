package com.mind.assistant.service;
import com.mind.assistant.entity.AssessmentRecord;
import com.mind.assistant.mapper.AssessmentRecordMapper;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mind.assistant.dto.SubmitDTO;
import com.mind.assistant.dto.Scale;
import com.mind.assistant.dto.ScaleOption;
import com.mind.assistant.dto.ScaleQuestion;
import com.mind.assistant.security.UserContext;
import com.mind.assistant.exception.BizException;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 心理测评服务：计分 -> 判级 -> 生成建议 -> 保存记录
 */
@Service
public class AssessmentService {

    private final AssessmentRecordMapper recordMapper;

    public AssessmentService(AssessmentRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    /**
     * 提交测评：校验 -> 逐题计分 -> 汇总分判级 -> 生成建议 -> 保存并返回
     */
    public ResultVO submit(SubmitDTO dto) {
        Scale scale = ScaleLibrary.get(dto.getScaleId());
        if (scale == null) {
            throw new BizException("量表不存在: " + dto.getScaleId());
        }
        List<ScaleQuestion> questions = scale.getQuestions();
        List<ScaleOption> options = scale.getOptions();
        if (dto.getAnswers().size() != questions.size()) {
            throw new BizException("答案数量与题目数量不符，该量表共 " + questions.size() + " 题");
        }

        // 逐题计分：正向题按选项分值，反向题 = 最高分 - 选项分值
        int maxScore = options.get(options.size() - 1).getScore();
        int rawScore = 0;
        List<AnswerDetail> details = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            int index = dto.getAnswers().get(i);
            if (index < 0 || index >= options.size()) {
                throw new BizException("第 " + (i + 1) + " 题的选项下标越界");
            }
            ScaleOption option = options.get(index);
            int score = questions.get(i).isReverse() ? (maxScore - option.getScore()) : option.getScore();
            rawScore += score;
            details.add(new AnswerDetail(i + 1, questions.get(i).getText(), option.getLabel(), score));
        }

        // 标准分与等级判定（SDS/SAS 需要 ×1.25 转换为标准分，其余直接用原始分）
        int totalScore = rawScore;
        switch (scale.getId()) {
            case "SDS", "SAS" -> totalScore = (int) Math.round(rawScore * 1.25);
            default -> { /* 其他量表直接用原始总分 */ }
        }

        LevelRule rule = levelOf(scale.getId(), totalScore);

        // 组装逐题明细 JSON
        DetailVO detail = new DetailVO();
        detail.setScaleId(scale.getId());
        detail.setScaleName(scale.getName());
        detail.setRawScore(rawScore);
        detail.setTotalScore(totalScore);
        detail.setAnswers(details);

        // 保存记录
        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(UserContext.getUserId());
        record.setScaleId(scale.getId());
        record.setScaleName(scale.getName());
        record.setTotalScore(totalScore);
        record.setLevel(rule.level());
        record.setSuggestion(rule.suggestion());
        record.setJsonDetail(JSONUtil.toJsonStr(detail));
        recordMapper.insert(record);

        // 返回结果
        ResultVO vo = new ResultVO();
        vo.setTotalScore(totalScore);
        vo.setLevel(rule.level());
        vo.setSuggestion(rule.suggestion());
        vo.setDetail(detail);
        return vo;
    }

    /**
     * 当前用户的测评历史（按时间倒序）
     */
    public List<AssessmentRecord> listRecords() {
        return recordMapper.selectList(new QueryWrapper<AssessmentRecord>()
                .eq("user_id", UserContext.getUserId())
                .orderByDesc("create_time"));
    }

    // ==================== 判级规则 ====================

    /**
     * 按量表编号判定等级并生成建议文案
     */
    private LevelRule levelOf(String scaleId, int total) {
        return switch (scaleId) {
            // SDS 标准分：<53 正常，53-62 轻度，63-72 中度，>=73 重度
            case "SDS" -> total < 53
                    ? new LevelRule("正常", "你的情绪状态整体平稳，继续保持规律作息、适度运动和良好的社交，做自己喜欢的事情吧。")
                    : total < 63
                    ? new LevelRule("轻度", "你可能存在一些轻度抑郁情绪。建议多与亲友倾诉、增加户外活动与光照，尝试写心情日记觉察情绪；若状态持续两周以上，建议寻求心理咨询。")
                    : total < 73
                    ? new LevelRule("中度", "你的抑郁情绪已比较明显，请不要独自硬扛。建议尽快预约学校心理中心或专业心理咨询师，并保持规律运动与作息；必要时到医院精神心理科评估。")
                    : new LevelRule("重度", "你的测评结果提示重度抑郁风险，请务必重视。请尽快前往医院精神心理科就诊，并拨打心理援助热线 12356 获取支持。你值得被认真对待，求助是勇敢的表现。");
            // SAS 标准分：<50 正常，50-59 轻度，60-69 中度，>=70 重度
            case "SAS" -> total < 50
                    ? new LevelRule("正常", "你的焦虑水平处于正常范围，继续保持平和心态。遇到压力时可以尝试深呼吸、正念冥想等放松方法。")
                    : total < 60
                    ? new LevelRule("轻度", "你可能存在轻度焦虑。建议识别压力源并拆解任务、规律运动、睡前减少咖啡因摄入；也可练习「4-7-8 呼吸法」帮助身体放松。")
                    : total < 70
                    ? new LevelRule("中度", "你的焦虑情绪已较明显，可能伴随失眠、心慌等躯体反应。建议尽快寻求心理咨询师帮助，同时保持规律作息；若躯体不适明显，请到医院检查排除生理原因。")
                    : new LevelRule("重度", "你的测评结果提示重度焦虑风险，请务必尽快到医院精神心理科就诊。紧急时可拨打心理援助热线 12356。焦虑是可以被治疗的，请相信专业的力量。");
            // PSS-10 总分 0-40：<=13 正常，14-26 中等压力，>=27 压力较大
            case "PSS" -> total <= 13
                    ? new LevelRule("正常", "你目前感知到的压力处于健康水平，继续保持良好的节奏。可以留意一下自己行之有效的减压方式，把它们固化成习惯。")
                    : total <= 26
                    ? new LevelRule("中等", "你感知到中等程度的压力。建议梳理优先级、学会说不，把大任务拆小；每天安排 20-30 分钟运动或放松时间，与信任的人保持联结。")
                    : new LevelRule("较大", "你感知到的压力较大，身心可能已经亮起黄灯。请优先保障睡眠与饮食，主动向家人朋友或咨询师求助；若已出现明显的躯体或情绪症状，请及时就医。");
            // PSQI 简版总分 0-15：<=4 好，5-9 一般，>=10 较差
            default -> total <= 4
                    ? new LevelRule("良好", "你的睡眠质量整体良好，继续保持规律作息与舒适的睡前环境。")
                    : total <= 9
                    ? new LevelRule("一般", "你的睡眠质量一般。建议固定起床时间、睡前一小时远离电子屏幕、白天适量运动但避免睡前剧烈运动；可尝试睡前正念呼吸练习。")
                    : new LevelRule("较差", "你的睡眠质量明显不佳，且可能影响白天的状态。请重视睡眠卫生，减少晚间咖啡因与酒精；若持续两周以上，建议到医院睡眠门诊或心理科咨询。");
        };
    }

    /** 判级结果（等级 + 建议） */
    private record LevelRule(String level, String suggestion) {
    }

    // ==================== 返回结构 ====================

    /** 测评结果返回结构 */
    @Data
    public static class ResultVO {
        private Integer totalScore;
        private String level;
        private String suggestion;
        private DetailVO detail;
    }

    /** 逐题明细 */
    @Data
    public static class DetailVO {
        private String scaleId;
        private String scaleName;
        private Integer rawScore;
        private Integer totalScore;
        private List<AnswerDetail> answers;
    }

    /** 单题作答明细 */
    @Data
    public static class AnswerDetail {
        private Integer index;
        private String question;
        private String option;
        private Integer score;

        public AnswerDetail(int index, String question, String option, int score) {
            this.index = index;
            this.question = question;
            this.option = option;
            this.score = score;
        }
    }
}
