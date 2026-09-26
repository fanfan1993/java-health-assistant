package com.mind.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 量表题目
 */
@Data
@AllArgsConstructor
public class ScaleQuestion {

    /** 题目文案 */
    private String text;

    /**
     * 是否反向计分题：
     * 正向题按选项分值直接计分；反向题计分 = 最高分 - 选项分值
     */
    private boolean reverse;
}
