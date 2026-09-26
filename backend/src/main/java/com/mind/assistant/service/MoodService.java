package com.mind.assistant.service;
import com.mind.assistant.mapper.MoodJournalMapper;

import com.mind.assistant.entity.MoodJournal;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mind.assistant.security.UserContext;
import com.mind.assistant.exception.BizException;
import com.mind.assistant.dto.MoodJournalDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 心情日记服务
 */
@Service
public class MoodService {

    private final MoodJournalMapper journalMapper;

    public MoodService(MoodJournalMapper journalMapper) {
        this.journalMapper = journalMapper;
    }

    /**
     * 分页查询当前用户日记（按创建时间倒序）
     */
    public Page<MoodJournal> page(long page, long size) {
        return journalMapper.selectPage(new Page<>(page, size),
                new QueryWrapper<MoodJournal>()
                        .eq("user_id", UserContext.getUserId())
                        .orderByDesc("create_time"));
    }

    /**
     * 新建日记：标签列表转逗号分隔存储
     */
    public MoodJournal create(MoodJournalDTO dto) {
        MoodJournal journal = new MoodJournal();
        journal.setUserId(UserContext.getUserId());
        journal.setContent(dto.getContent());
        journal.setMood(dto.getMood());
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            journal.setTags(String.join(",", dto.getTags()));
        }
        journalMapper.insert(journal);
        return journal;
    }

    /**
     * 删除日记（校验归属）
     */
    public void delete(Long id) {
        MoodJournal journal = journalMapper.selectById(id);
        if (journal == null || !journal.getUserId().equals(UserContext.getUserId())) {
            throw new BizException("日记不存在或无权删除");
        }
        journalMapper.deleteById(id);
    }

    /**
     * 标签字符串 -> List（供 VO 转换）
     */
    public static List<String> splitTags(String tags) {
        return StringUtils.hasText(tags) ? List.of(tags.split(",")) : List.of();
    }
}
