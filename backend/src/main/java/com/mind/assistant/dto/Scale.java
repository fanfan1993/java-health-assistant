package com.mind.assistant.dto;

import lombok.Data;

import java.util.List;

/**
 * 量表定义（内置静态数据，不放数据库）
 */
@Data
public class Scale {

    /** 量表编号：SDS / SAS / PSS / PSQI */
    private String id;

    /** 量表名称 */
    private String name;

    /** 量表说明 */
    private String description;

    /** 全部选项（按等级从低到高排列） */
    private List<ScaleOption> options;

    /** 全部题目 */
    private List<ScaleQuestion> questions;
}
