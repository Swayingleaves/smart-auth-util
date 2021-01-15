package com.swayingleaves.smartauthutil;

import com.alibaba.fastjson.JSONObject;
import com.swayingleaves.smartauthutil.aspect.LoginUser;
import com.swayingleaves.smartauthutil.util.AuthRedisKeyUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class SmartAuthUtilApplicationTests {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
        //设置Token
        final String replace = UUID.randomUUID().toString().replace("-", "");
        final String loginUserTokenKey = AuthRedisKeyUtil.getLoginUserTokenKey(replace);

        LoginUser loginUser = new LoginUser();
        LoginUser.User user = new LoginUser.User();
        String id = "1";
        user.setId(id);
        user.setNickName("张三");

        stringRedisTemplate.opsForValue().set(loginUserTokenKey, id);
        loginUser.setUser(user);

        List<LoginUser.Power> power = new ArrayList<>();
        LoginUser.Power power1 = new LoginUser.Power();
        power1.setRoleExplain("admin");

        List<LoginUser.Power.Permission> permissions = new ArrayList<>();
        LoginUser.Power.Permission permission = new LoginUser.Power.Permission();
        permission.setPmName("add");
        permissions.add(permission);
        LoginUser.Power.Permission permission1 = new LoginUser.Power.Permission();
        permission1.setPmName("del");
        permissions.add(permission1);
        power1.setPermissions(permissions);
        power.add(power1);
        loginUser.setPowers(power);

        List<LoginUser.Group> group = new ArrayList<>();
        LoginUser.Group group1 = new LoginUser.Group();
        group1.setGroupName("G1");
        group.add(group1);
        LoginUser.Group group2 = new LoginUser.Group();
        group2.setGroupName("G2");
        group.add(group2);

        loginUser.setGroups(group);

        String loginUserIdKey = AuthRedisKeyUtil.getLoginUserIdKey(id);
        stringRedisTemplate.opsForValue().set(loginUserIdKey, JSONObject.toJSONString(loginUser));
    }

}
