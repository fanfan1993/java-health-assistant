package com.mind.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 量表选项（一个等级）
 */
@Data
@AllArgsConstructor
public class ScaleOption {

    /** 选项文案，如「没有或很少时间」 */
    private String label;

    /** 该选项对应的分值 */
    private int score;
}
