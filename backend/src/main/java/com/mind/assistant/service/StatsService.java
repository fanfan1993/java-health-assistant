package com.mind.assistant.service;
import com.mind.assistant.vo.OverviewVO;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mind.assistant.entity.AssessmentRecord;
import com.mind.assistant.mapper.AssessmentRecordMapper;
import com.mind.assistant.security.UserContext;
import com.mind.assistant.entity.ChatMessage;
import com.mind.assistant.mapper.ChatMessageMapper;
import com.mind.assistant.entity.ChatSession;
import com.mind.assistant.mapper.ChatSessionMapper;
import com.mind.assistant.entity.MoodJournal;
import com.mind.assistant.mapper.MoodJournalMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页统计服务：
 * 数据量小，查询后内存聚合，代码保持清晰易读
 */
@Service
public class StatsService {

    private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");
    /** 心情 1-5 对应的中文名称 */
    private static final String[] EMOTION_NAMES = {"低落", "一般", "平静", "愉悦", "开心"};

    private final MoodJournalMapper journalMapper;
    private final AssessmentRecordMapper recordMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    public StatsService(MoodJournalMapper journalMapper, AssessmentRecordMapper recordMapper,
                        ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper) {
        this.journalMapper = journalMapper;
        this.recordMapper = recordMapper;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    /**
     * 首页总览数据
     */
    public OverviewVO overview() {
        Long userId = UserContext.getUserId();
        OverviewVO vo = new OverviewVO();
        vo.setMoodTrend(buildMoodTrend(userId));
        vo.setEmotionDistribution(buildEmotionDistribution(userId));
        vo.setAssessmentHistory(buildAssessmentHistory(userId));
        vo.setChatCount(countChatMessages(userId));
        vo.setJournalCount(countJournal(userId));
        vo.setAvgMood(calcAvgMood(userId));
        vo.setStreakDays(calcStreakDays(userId));
        return vo;
    }

    /**
     * 最近 14 天心情趋势：每天平均 mood，无数据补 0
     */
    private List<OverviewVO.MoodPoint> buildMoodTrend(Long userId) {
        LocalDateTime since = LocalDate.now().minusDays(13).atStartOfDay();
        List<MoodJournal> journals = journalMapper.selectList(new QueryWrapper<MoodJournal>()
                .select("mood", "create_time")
                .eq("user_id", userId)
                .ge("create_time", since));

        // 按日期分组求平均
        Map<LocalDate, Double> avgByDate = journals.stream()
                .collect(Collectors.groupingBy(j -> j.getCreateTime().toLocalDate(),
                        Collectors.averagingInt(MoodJournal::getMood)));

        // 14 天逐日输出，无数据补 0
        List<OverviewVO.MoodPoint> trend = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            trend.add(new OverviewVO.MoodPoint(date.format(MM_DD),
                    avgByDate.getOrDefault(date, 0.0)));
        }
        return trend;
    }

    /**
     * 心情分布：mood 1-5 各计数
     */
    private List<OverviewVO.EmotionCount> buildEmotionDistribution(Long userId) {
        List<MoodJournal> all = journalMapper.selectList(new QueryWrapper<MoodJournal>()
                .select("mood")
                .eq("user_id", userId));
        Map<Integer, Long> countByMood = all.stream()
                .collect(Collectors.groupingBy(MoodJournal::getMood, Collectors.counting()));

        List<OverviewVO.EmotionCount> distribution = new ArrayList<>();
        for (int mood = 1; mood <= 5; mood++) {
            distribution.add(new OverviewVO.EmotionCount(EMOTION_NAMES[mood - 1],
                    countByMood.getOrDefault(mood, 0L)));
        }
        return distribution;
    }

    /**
     * 最近 5 条测评记录
     */
    private List<OverviewVO.AssessmentBrief> buildAssessmentHistory(Long userId) {
        List<AssessmentRecord> records = recordMapper.selectList(new QueryWrapper<AssessmentRecord>()
                .select("scale_name", "total_score", "level", "create_time")
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .last("limit 5"));
        return records.stream().map(r -> {
            OverviewVO.AssessmentBrief brief = new OverviewVO.AssessmentBrief();
            brief.setScaleName(r.getScaleName());
            brief.setTotalScore(r.getTotalScore());
            brief.setLevel(r.getLevel());
            brief.setCreateTime(r.getCreateTime());
            return brief;
        }).toList();
    }

    /**
     * 对话消息数：当前用户所有会话中自己发送的消息条数
     */
    private Long countChatMessages(Long userId) {
        List<ChatSession> sessions = sessionMapper.selectList(new QueryWrapper<ChatSession>()
                .select("id")
                .eq("user_id", userId));
        if (sessions.isEmpty()) {
            return 0L;
        }
        List<Long> sessionIds = sessions.stream().map(ChatSession::getId).toList();
        return messageMapper.selectCount(new QueryWrapper<ChatMessage>()
                .in("session_id", sessionIds)
                .eq("role", "user"));
    }

    /**
     * 日记条数
     */
    private Long countJournal(Long userId) {
        return journalMapper.selectCount(new QueryWrapper<MoodJournal>().eq("user_id", userId));
    }

    /**
     * 平均心情（保留 1 位小数），无日记时返回 0.0
     */
    private Double calcAvgMood(Long userId) {
        List<MoodJournal> all = journalMapper.selectList(new QueryWrapper<MoodJournal>()
                .select("mood")
                .eq("user_id", userId));
        if (all.isEmpty()) {
            return 0.0;
        }
        double avg = all.stream().mapToInt(MoodJournal::getMood).average().orElse(0.0);
        return Math.round(avg * 10) / 10.0;
    }

    /**
     * 连续记录天数：从今天（或昨天）开始，按有日记的日期倒推连续计数
     */
    private Integer calcStreakDays(Long userId) {
        List<MoodJournal> all = journalMapper.selectList(new QueryWrapper<MoodJournal>()
                .select("create_time")
                .eq("user_id", userId));
        if (all.isEmpty()) {
            return 0;
        }
        // 去重后的日期，倒序排列
        List<LocalDate> dates = all.stream()
                .map(j -> j.getCreateTime().toLocalDate())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        LocalDate today = LocalDate.now();
        // 最近一次记录必须是今天或昨天，否则连续中断
        LocalDate cursor = dates.get(0);
        if (!cursor.equals(today) && !cursor.equals(today.minusDays(1))) {
            return 0;
        }
        int streak = 0;
        for (LocalDate date : dates) {
            if (date.equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }
}
