package com.swayingleaves.smartauthutil.aspect;

import com.swayingleaves.smartauthutil.annotation.CheckPermission;
import com.swayingleaves.smartauthutil.code.Const;
import com.swayingleaves.smartauthutil.exception.NoAuthorityException;
import com.swayingleaves.smartauthutil.util.UserInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author zhenglin
 * @since 2019/9/16 14:39
 * @apiNote 检查权限注解实现类
 */
@Aspect
@Component
@Slf4j
@Order(3)
public class CheckPermissionAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Before("execution(* *..controller..*(..))")
    public void before(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckPermission annotation = method.getAnnotation(CheckPermission.class);

        if (annotation != null) {
            String[] checkPermissions = annotation.permissions();
            if (checkPermissions.length != 0){
                String op = annotation.opt().name();
                //获取到请求的属性
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                //获取到请求对象
                HttpServletRequest request = attributes.getRequest();

                Object loginUserId = request.getAttribute(Const.LOGIN_USER_ID);
                Map<String,List<String>> power = UserInfoUtil.getPower(request,loginUserId);
                boolean flag = false;
                switch (op){
                    case Const.AND: flag = check(checkPermissions,true,power) ;break;
                    case Const.OR:
                    default:flag = check(checkPermissions,false, power) ;break;
                }
                if(!flag){
                    throw new NoAuthorityException(loginUserId,"未授权该权限");
                }
            }
        }
    }

    private boolean check(String[] permissions, boolean matchAll, Map<String,List<String>> power){
        List<String> permissionList = Arrays.asList(permissions);
        Map<String, List<String>> permissionMapping = new HashMap<>(16);
        for (String data : permissionList) {
            final String[] split = data.split(":");
            final String role = split[0];
            final String permission = split[1];
            if (permissionMapping.containsKey(role)) {
                final List<String> needPermissions = permissionMapping.get(role);
                needPermissions.add(permission);
            }else {
                List<String> newList = new ArrayList<>();
                newList.add(permission);
                permissionMapping.put(role,newList);
            }
        }
        if (matchAll){
            for (Map.Entry<String, List<String>> entry : permissionMapping.entrySet()) {
                final String key = entry.getKey();
                final List<String> value = entry.getValue();
                if (power.containsKey(key)) {
                    final List<String> strings = power.get(key);
                    for (String s : value) {
                        if (!strings.contains(s)){
                            return false;
                        }
                    }
                }else {
                    return false;
                }
            }
        }else {
            for (Map.Entry<String, List<String>> entry : permissionMapping.entrySet()) {
                final String role = entry.getKey();
                final List<String> needPermissions = entry.getValue();
                if (power.containsKey(role)) {
                    final int size = needPermissions.size();
                    final List<String> hasPermissions = power.get(role);
                    if (size > 1){
                        for (String s : needPermissions) {
                            if (hasPermissions.contains(s)) {
                                return true;
                            }
                        }
                    }else {
                        if (hasPermissions.containsAll(needPermissions)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
