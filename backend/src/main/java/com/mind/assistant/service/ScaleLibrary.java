package com.mind.assistant.service;

import com.mind.assistant.dto.Scale;
import com.mind.assistant.dto.ScaleOption;
import com.mind.assistant.dto.ScaleQuestion;

import java.util.List;

/**
 * 内置心理量表库（静态数据，代码中维护，不放数据库）：
 * SDS 抑郁自评（20 题 4 级）、SAS 焦虑自评（20 题 4 级）、
 * PSS 压力知觉（10 题 5 级）、PSQI 简版睡眠（5 题）
 */
public class ScaleLibrary {

    private static final List<Scale> SCALES = List.of(buildSds(), buildSas(), buildPss(), buildPsqi());

    private ScaleLibrary() {
    }

    /** 全部量表 */
    public static List<Scale> all() {
        return SCALES;
    }

    /** 按编号获取量表 */
    public static Scale get(String scaleId) {
        return SCALES.stream()
                .filter(s -> s.getId().equalsIgnoreCase(scaleId))
                .findFirst()
                .orElse(null);
    }

    // ==================== SDS 抑郁自评量表 ====================

    private static Scale buildSds() {
        // 4 级选项（1-4 分）
        List<ScaleOption> options = List.of(
                new ScaleOption("没有或很少时间", 1),
                new ScaleOption("少部分时间", 2),
                new ScaleOption("相当多时间", 3),
                new ScaleOption("绝大部分时间或全部时间", 4));
        // 20 题（第 2,5,6,11,12,14,16,17,18,20 题为反向计分）
        List<ScaleQuestion> questions = List.of(
                new ScaleQuestion("我觉得闷闷不乐，情绪低沉", false),
                new ScaleQuestion("我觉得一天之中早晨最好", true),
                new ScaleQuestion("我一阵阵地哭出来或想哭", false),
                new ScaleQuestion("我晚上睡眠不好", false),
                new ScaleQuestion("我吃得跟平常一样多", true),
                new ScaleQuestion("我与异性亲密接触时和以往一样感到愉快", true),
                new ScaleQuestion("我发觉我的体重在下降", false),
                new ScaleQuestion("我有便秘的苦恼", false),
                new ScaleQuestion("我心跳比平常快", false),
                new ScaleQuestion("我无缘无故地感到疲乏", false),
                new ScaleQuestion("我的头脑跟平常一样清楚", true),
                new ScaleQuestion("我觉得经常做的事情并没有困难", true),
                new ScaleQuestion("我觉得不安而平静不下来", false),
                new ScaleQuestion("我对将来抱有希望", true),
                new ScaleQuestion("我比平常容易生气激动", false),
                new ScaleQuestion("我觉得作出决定是容易的", true),
                new ScaleQuestion("我觉得自己是个有用的人，有人需要我", true),
                new ScaleQuestion("我的生活过得很有意思", true),
                new ScaleQuestion("我认为如果我死了别人会生活得好些", false),
                new ScaleQuestion("平常感兴趣的事我仍然照样感兴趣", true));
        Scale scale = new Scale();
        scale.setId("SDS");
        scale.setName("抑郁自评量表（SDS）");
        scale.setDescription("由 Zung 编制的抑郁自评量表，共 20 题，4 级评分，用于评估最近一周的抑郁状态。作答时请根据最近一周的实际感受选择。");
        scale.setOptions(options);
        scale.setQuestions(questions);
        return scale;
    }

    // ==================== SAS 焦虑自评量表 ====================

    private static Scale buildSas() {
        List<ScaleOption> options = List.of(
                new ScaleOption("没有或很少时间", 1),
                new ScaleOption("少部分时间", 2),
                new ScaleOption("相当多时间", 3),
                new ScaleOption("绝大部分时间或全部时间", 4));
        // 20 题（第 5,9,13,17,19 题为反向计分）
        List<ScaleQuestion> questions = List.of(
                new ScaleQuestion("我觉得比平常容易紧张和着急", false),
                new ScaleQuestion("我无缘无故地感到害怕", false),
                new ScaleQuestion("我容易心里烦乱或觉得惊恐", false),
                new ScaleQuestion("我觉得我可能将要发疯", false),
                new ScaleQuestion("我觉得一切都很好，也不会发生什么不幸", true),
                new ScaleQuestion("我手脚发抖打颤", false),
                new ScaleQuestion("我因为头痛、头颈痛和背痛而苦恼", false),
                new ScaleQuestion("我感觉容易衰弱和疲乏", false),
                new ScaleQuestion("我觉得心平气和，并且容易安静坐着", true),
                new ScaleQuestion("我觉得心跳得很快", false),
                new ScaleQuestion("我因为一阵阵头晕而苦恼", false),
                new ScaleQuestion("我有晕倒发作或觉得要晕倒似的", false),
                new ScaleQuestion("我呼气吸气都感到很容易", true),
                new ScaleQuestion("我手脚麻木和刺痛", false),
                new ScaleQuestion("我因为胃痛和消化不良而苦恼", false),
                new ScaleQuestion("我常常要小便", false),
                new ScaleQuestion("我的手常常是干燥温暖的", true),
                new ScaleQuestion("我脸红发热", false),
                new ScaleQuestion("我容易入睡并且一夜睡得很好", true),
                new ScaleQuestion("我做噩梦", false));
        Scale scale = new Scale();
        scale.setId("SAS");
        scale.setName("焦虑自评量表（SAS）");
        scale.setDescription("由 Zung 编制的焦虑自评量表，共 20 题，4 级评分，用于评估最近一周的焦虑状态。作答时请根据最近一周的实际感受选择。");
        scale.setOptions(options);
        scale.setQuestions(questions);
        return scale;
    }

    // ==================== PSS 压力知觉量表 ====================

    private static Scale buildPss() {
        // 5 级选项（0-4 分）
        List<ScaleOption> options = List.of(
                new ScaleOption("从不", 0),
                new ScaleOption("偶尔", 1),
                new ScaleOption("有时", 2),
                new ScaleOption("时常", 3),
                new ScaleOption("总是", 4));
        // 10 题（第 4,5,7,8 题为反向计分）
        List<ScaleQuestion> questions = List.of(
                new ScaleQuestion("因为发生了意料之外的事情，我感到心烦意乱", false),
                new ScaleQuestion("我感觉到自己无法控制生活中重要的事情", false),
                new ScaleQuestion("我感到紧张不安和有压力", false),
                new ScaleQuestion("我成功地处理了恼人的生活琐事", true),
                new ScaleQuestion("我感觉到自己能有效地应对生活中发生的重要变化", true),
                new ScaleQuestion("我感觉到对自己处理个人问题的能力很有信心", true),
                new ScaleQuestion("我感觉事情按照自己希望的方式发展", true),
                new ScaleQuestion("我发现自己无法应付所有不得不做的事情", false),
                new ScaleQuestion("我能够控制自己生活中的恼人事情", true),
                new ScaleQuestion("我感觉困难堆积如山，无法克服", false));
        Scale scale = new Scale();
        scale.setId("PSS");
        scale.setName("压力知觉量表（PSS-10）");
        scale.setDescription("由 Cohen 等编制的压力知觉量表，共 10 题，5 级评分，用于评估最近一个月你对生活压力的感知程度。");
        scale.setOptions(options);
        scale.setQuestions(questions);
        return scale;
    }

    // ==================== PSQI 简版睡眠质量量表 ====================

    private static Scale buildPsqi() {
        // 4 级选项（0-3 分）
        List<ScaleOption> options = List.of(
                new ScaleOption("很好", 0),
                new ScaleOption("较好", 1),
                new ScaleOption("较差", 2),
                new ScaleOption("很差", 3));
        List<ScaleQuestion> questions = List.of(
                new ScaleQuestion("总体而言，您认为自己的睡眠质量怎么样？", false),
                new ScaleQuestion("您入睡所需要的时间长吗（是否经常超过 30 分钟）？", false),
                new ScaleQuestion("您夜间是否会因各种原因（易醒、早醒、起夜等）而睡眠中断？", false),
                new ScaleQuestion("您早晨醒来后是否感到精力充沛？", true),
                new ScaleQuestion("您是否因为睡眠问题而影响白天的精神状态或情绪？", false));
        Scale scale = new Scale();
        scale.setId("PSQI");
        scale.setName("睡眠质量简评（PSQI 简版）");
        scale.setDescription("基于匹兹堡睡眠质量指数简化而来，共 5 题，4 级评分，用于快速评估最近一个月的睡眠质量。");
        scale.setOptions(options);
        scale.setQuestions(questions);
        return scale;
    }
}
