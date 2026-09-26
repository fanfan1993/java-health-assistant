package com.mind.assistant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 心情日记实体
 */
@Data
@TableName("mood_journal")
public class MoodJournal {

    /** 日记 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 日记内容 */
    private String content;

    /** 心情 1-5（1 低落 ~ 5 开心） */
    private Integer mood;

    /** 标签（逗号分隔） */
    private String tags;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
