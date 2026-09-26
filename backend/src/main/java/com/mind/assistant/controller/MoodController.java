package com.mind.assistant.controller;
import com.mind.assistant.entity.MoodJournal;
import com.mind.assistant.service.MoodService;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mind.assistant.common.Result;
import com.mind.assistant.dto.MoodJournalDTO;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 心情日记接口
 */
@Validated
@RestController
@RequestMapping("/api/mood")
public class MoodController {

    private final MoodService moodService;

    public MoodController(MoodService moodService) {
        this.moodService = moodService;
    }

    /**
     * 分页查询日记：GET /api/mood/journal?page=1&size=10
     */
    @GetMapping("/journal")
    public Result<PageVO> page(@RequestParam(defaultValue = "1") long page,
                               @RequestParam(defaultValue = "10") long size) {
        Page<MoodJournal> result = moodService.page(page, size);
        PageVO vo = new PageVO();
        vo.setTotal(result.getTotal());
        vo.setPage(page);
        vo.setSize(size);
        vo.setRecords(result.getRecords().stream().map(MoodController::toVO).toList());
        return Result.success(vo);
    }

    /**
     * 新建日记：POST /api/mood/journal
     */
    @PostMapping("/journal")
    public Result<JournalVO> create(@Valid @RequestBody MoodJournalDTO dto) {
        return Result.success(toVO(moodService.create(dto)));
    }

    /**
     * 删除日记：DELETE /api/mood/journal/{id}
     */
    @DeleteMapping("/journal/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        moodService.delete(id);
        return Result.success();
    }

    /**
     * 实体 -> VO：tags 拆分为数组
     */
    private static JournalVO toVO(MoodJournal journal) {
        JournalVO vo = new JournalVO();
        vo.setId(journal.getId());
        vo.setContent(journal.getContent());
        vo.setMood(journal.getMood());
        vo.setTags(MoodService.splitTags(journal.getTags()));
        vo.setCreateTime(journal.getCreateTime());
        return vo;
    }

    /** 分页返回结构 */
    @Data
    public static class PageVO {
        private Long total;
        private Long page;
        private Long size;
        private List<JournalVO> records;
    }

    /** 日记 VO：tags 以数组返回 */
    @Data
    public static class JournalVO {
        private Long id;
        private String content;
        private Integer mood;
        private List<String> tags;
        private LocalDateTime createTime;
    }
}
