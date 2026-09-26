package com.mind.assistant.controller;
import com.mind.assistant.entity.AssessmentRecord;
import com.mind.assistant.service.AssessmentService;
import com.mind.assistant.service.ScaleLibrary;


import com.mind.assistant.dto.SubmitDTO;
import com.mind.assistant.dto.Scale;
import com.mind.assistant.common.Result;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 心理测评接口
 */
@Validated
@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * 量表列表（含题目与选项分值）：GET /api/assessment/scales
     */
    @GetMapping("/scales")
    public Result<List<Scale>> scales() {
        return Result.success(ScaleLibrary.all());
    }

    /**
     * 提交测评：POST /api/assessment/submit
     */
    @PostMapping("/submit")
    public Result<AssessmentService.ResultVO> submit(@Valid @RequestBody SubmitDTO dto) {
        return Result.success(assessmentService.submit(dto));
    }

    /**
     * 当前用户测评历史：GET /api/assessment/records
     */
    @GetMapping("/records")
    public Result<List<AssessmentRecord>> records() {
        return Result.success(assessmentService.listRecords());
    }
}
