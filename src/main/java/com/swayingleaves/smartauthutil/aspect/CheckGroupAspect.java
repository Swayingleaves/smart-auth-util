package com.swayingleaves.smartauthutil.aspect;

import com.swayingleaves.smartauthutil.annotation.CheckGroup;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author zhenglin
 * @apiNote 检查组注解实现类
 * @since 2020/2/21 5:05 下午
 */
@Aspect
@Component
@Slf4j
@Order(2)
public class CheckGroupAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Before("execution(* *..controller..*(..))")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckGroup annotation = method.getAnnotation(CheckGroup.class);

        if (annotation == null) {
            //获取类上注解
            annotation = joinPoint.getTarget().getClass().getAnnotation(CheckGroup.class);
        }
        if (annotation != null) {
            //获取到请求对象
            final String[] group = annotation.group();
            if (group.length != 0) {
                final String optName = annotation.opt().name();
                final LoginUser loginUser = LoginUserHolder.get();
                final String loginUserId = loginUser.getUser().getId();

                final List<LoginUser.Group> groups1 = loginUser.getGroups();
                final List<String> groups = groups1.stream().map(LoginUser.Group::getGroupName).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(groups)) {
                    throw new NoAuthorityException(loginUserId, "未授权该组");
                }
                boolean result = false;
                switch (optName) {
                    case Const.OR:
                        for (String s : group) {
                            if (groups.contains(s)) {
                                result = true;
                                break;
                            }
                        }
                        break;
                    case Const.AND:
                        result = groups.containsAll(Arrays.asList(group));
                    default:
                        break;
                }
                if (!result) {
                    throw new NoAuthorityException(loginUserId, "未授权该组");
                }
            }
        }
    }

    @After("execution(* *..controller..*(..))")
    public void after(){
        LoginUserHolder.remove();
    }
}
