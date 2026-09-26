package com.mind.assistant.vo;
import com.mind.assistant.entity.User;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象（不含密码）
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private LocalDateTime createTime;

    /**
     * 实体 -> VO（剔除密码字段）
     */
    public static UserVO from(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
