package com.mind.assistant.controller;
import com.mind.assistant.security.JwtInterceptor;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mind.assistant.entity.AssessmentRecord;
import com.mind.assistant.mapper.AssessmentRecordMapper;
import com.mind.assistant.entity.ChatMessage;
import com.mind.assistant.mapper.ChatMessageMapper;
import com.mind.assistant.common.Result;
import com.mind.assistant.entity.MoodJournal;
import com.mind.assistant.mapper.MoodJournalMapper;
import com.mind.assistant.entity.User;
import com.mind.assistant.mapper.UserMapper;
import com.mind.assistant.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端接口：
 * 仅 ADMIN 角色可用（/api/admin/** 的角色校验在 JwtInterceptor 中完成）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserMapper userMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final MoodJournalMapper journalMapper;
    private final AssessmentRecordMapper recordMapper;

    public AdminController(UserMapper userMapper, ChatMessageMapper chatMessageMapper,
                           MoodJournalMapper journalMapper, AssessmentRecordMapper recordMapper) {
        this.userMapper = userMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.journalMapper = journalMapper;
        this.recordMapper = recordMapper;
    }

    /**
     * 用户列表：GET /api/admin/users
     */
    @GetMapping("/users")
    public Result<List<UserVO>> users() {
        List<User> users = userMapper.selectList(
                new QueryWrapper<User>().orderByAsc("id"));
        return Result.success(users.stream().map(UserVO::from).toList());
    }

    /**
     * 平台总览：GET /api/admin/overview
     */
    @GetMapping("/overview")
    public Result<OverviewVO> overview() {
        OverviewVO vo = new OverviewVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setChatCount(chatMessageMapper.selectCount(null));
        vo.setJournalCount(journalMapper.selectCount(null));
        vo.setAssessmentCount(recordMapper.selectCount(null));
        return Result.success(vo);
    }

    /** 平台总览 VO */
    @lombok.Data
    public static class OverviewVO {
        /** 用户总数 */
        private Long userCount;
        /** 对话消息总数 */
        private Long chatCount;
        /** 日记总数 */
        private Long journalCount;
        /** 测评总数 */
        private Long assessmentCount;
    }
}
