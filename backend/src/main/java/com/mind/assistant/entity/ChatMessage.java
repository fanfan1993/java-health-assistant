package com.mind.assistant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话消息实体
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    /** 消息 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属会话 ID */
    private Long sessionId;

    /** 角色：user / assistant */
    private String role;

    /** 消息内容 */
    private String content;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
