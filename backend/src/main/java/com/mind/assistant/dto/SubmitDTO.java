package com.mind.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 测评提交参数
 */
@Data
public class SubmitDTO {

    /** 量表编号：SDS / SAS / PSS / PSQI */
    @NotBlank(message = "量表编号不能为空")
    private String scaleId;

    /** 每题作答的选项下标（从 0 开始，顺序与题目一致） */
    @NotEmpty(message = "答案不能为空")
    private List<Integer> answers;
}
