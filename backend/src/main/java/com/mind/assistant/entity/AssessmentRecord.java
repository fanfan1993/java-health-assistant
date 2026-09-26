package com.mind.assistant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 心理测评记录实体
 */
@Data
@TableName("assessment_record")
public class AssessmentRecord {

    /** 记录 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 量表编号：SDS / SAS / PSS / PSQI */
    private String scaleId;

    /** 量表名称 */
    private String scaleName;

    /** 总分 */
    private Integer totalScore;

    /** 等级：正常 / 轻度 / 中度 / 重度 等 */
    private String level;

    /** 建议文案 */
    private String suggestion;

    /** 逐题作答明细（JSON） */
    private String jsonDetail;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
