package com.mind.assistant.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新建心情日记参数
 */
@Data
public class MoodJournalDTO {

    /** 日记内容 */
    @NotBlank(message = "日记内容不能为空")
    private String content;

    /** 心情 1-5（1 低落 ~ 5 开心） */
    @NotNull(message = "心情值不能为空")
    @Min(value = 1, message = "心情值最小为 1")
    @Max(value = 5, message = "心情值最大为 5")
    private Integer mood;

    /** 标签（可选，如：工作、运动） */
    @Size(max = 10, message = "标签最多 10 个")
    private List<String> tags;
}
