package com.mind.assistant.controller;
import com.mind.assistant.vo.OverviewVO;
import com.mind.assistant.service.StatsService;


import com.mind.assistant.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页统计接口
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 首页数据总览：GET /api/stats/overview
     */
    @GetMapping("/overview")
    public Result<OverviewVO> overview() {
        return Result.success(statsService.overview());
    }
}
