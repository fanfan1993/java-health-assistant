package com.mind.assistant.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 首页数据总览 VO
 */
@Data
public class OverviewVO {

    /** 最近 14 天心情趋势 */
    private List<MoodPoint> moodTrend;

    /** 心情分布（1-5 各计数） */
    private List<EmotionCount> emotionDistribution;

    /** 最近 5 条测评记录 */
    private List<AssessmentBrief> assessmentHistory;

    /** 对话消息数（用户发送的消息条数） */
    private Long chatCount;

    /** 日记条数 */
    private Long journalCount;

    /** 平均心情（保留 1 位小数） */
    private Double avgMood;

    /** 连续记录天数 */
    private Integer streakDays;

    /** 心情趋势点 */
    @Data
    public static class MoodPoint {
        /** 日期，格式 MM-dd */
        private String date;
        /** 当天平均心情（保留 1 位小数，无数据补 0） */
        private Double score;

        public MoodPoint(String date, Double score) {
            this.date = date;
            this.score = score;
        }
    }

    /** 心情分布项 */
    @Data
    public static class EmotionCount {
        /** 名称：低落/一般/平静/愉悦/开心 */
        private String name;
        /** 计数 */
        private Long value;

        public EmotionCount(String name, Long value) {
            this.name = name;
            this.value = value;
        }
    }

    /** 测评摘要 */
    @Data
    public static class AssessmentBrief {
        private String scaleName;
        private Integer totalScore;
        private String level;
        private LocalDateTime createTime;
    }
}
