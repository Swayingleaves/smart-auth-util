package com.swayingleaves.smartauthutil.aspect;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhenglin
 * @since 2020/9/3 2:28 下午
 * @apiNote 登录用户实体抽象
 */
@Data
public class LoginUser {
    /**
     * 用户信息封装
     */
    private User user;
    /**
     * 用户组信息封装
     */
    private List<Group> groups;
    /**
     * 用户权限封装
     */
    private List<Power> powers;
    /**
     * 登录sso-token
     */
    private String token;

    @Data
    public static class User{
        private String id;
        private String nickName;
        private String mail;
        private String mobile;
        private Integer lock;
        private Integer identification;
    }

    @Data
    public static class Group{
        private String groupName;
        private String groupExplain;
    }

    @Data
    public static class Power{
        private String roleName;
        private String roleExplain;
        private Integer roleWeight;
        private List<Permission> permissions;

        @Data
        public static class Permission{
            private String pmName;
            private String pmExplain;
        }
    }
}
