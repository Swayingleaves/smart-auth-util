package com.swayingleaves.smartauthutil.aspect;

import com.swayingleaves.smartauthutil.annotation.CheckPermission;
import com.swayingleaves.smartauthutil.code.Const;
import com.swayingleaves.smartauthutil.code.LoginUserHolder;
import com.swayingleaves.smartauthutil.exception.NoAuthorityException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhenglin
 * @apiNote 检查权限注解实现类
 * @since 2019/9/16 14:39
 */
@Aspect
@Component
@Slf4j
@Order(3)
public class CheckPermissionAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Before("execution(* *..controller..*(..))")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckPermission annotation = method.getAnnotation(CheckPermission.class);

        if (annotation != null) {
            String[] checkPermissions = annotation.permissions();
            if (checkPermissions.length != 0) {
                String op = annotation.opt().name();
                final LoginUser loginUser = LoginUserHolder.get();
                final String loginUserId = loginUser.getUser().getId();

                final List<LoginUser.Power> powers = loginUser.getPowers();

                boolean flag;
                switch (op) {
                    case Const.AND:
                        flag = check(checkPermissions, true, powers);
                        break;
                    case Const.OR:
                    default:
                        flag = check(checkPermissions, false, powers);
                        break;
                }
                if (!flag) {
                    throw new NoAuthorityException(loginUserId, "未授权该权限");
                }
            }
        }
    }

    private boolean check(String[] permissions, boolean matchAll, List<LoginUser.Power> powers) {
        Map<String, List<String>> permissionMapping = new HashMap<>(16);
        for (String data : permissions) {
            final String[] split = data.split(":");
            final String role = split[0];
            final String permission = split[1];
            if (permissionMapping.containsKey(role)) {
                final List<String> needPermissions = permissionMapping.get(role);
                needPermissions.add(permission);
            } else {
                List<String> newList = new ArrayList<>();
                newList.add(permission);
                permissionMapping.put(role, newList);
            }
        }
        Map<String, List<String>> userHas = new HashMap<>(16);
        for (LoginUser.Power power : powers) {
            final String roleName = power.getRoleName();
            final List<LoginUser.Power.Permission> permissions1 = power.getPermissions();
            final List<String> collect = permissions1.stream().map(LoginUser.Power.Permission::getPmName).collect(Collectors.toList());
            userHas.put(roleName, collect);
        }
        if (matchAll) {
            for (Map.Entry<String, List<String>> entry : permissionMapping.entrySet()) {
                final String role = entry.getKey();
                final List<String> value = entry.getValue();
                if (userHas.containsKey(role)) {
                    final List<String> strings = userHas.get(role);
                    for (String s : value) {
                        if (!strings.contains(s)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        } else {
            for (Map.Entry<String, List<String>> entry : permissionMapping.entrySet()) {
                final String role = entry.getKey();
                final List<String> needPermissions = entry.getValue();
                if (userHas.containsKey(role)) {
                    final int size = needPermissions.size();
                    final List<String> hasPermissions = userHas.get(role);
                    if (size > 1) {
                        for (String s : needPermissions) {
                            if (hasPermissions.contains(s)) {
                                return true;
                            }
                        }
                    } else {
                        if (hasPermissions.containsAll(needPermissions)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @After("execution(* *..controller..*(..))")
    public void after(){
        LoginUserHolder.remove();
    }
}
