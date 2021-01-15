package com.swayingleaves.smartauthutil.aspect;

import com.swayingleaves.smartauthutil.annotation.CheckRole;
import com.swayingleaves.smartauthutil.code.Const;
import com.swayingleaves.smartauthutil.code.LoginUserHolder;
import com.swayingleaves.smartauthutil.exception.NoAuthorityException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhenglin
 * @since 2019/9/16 14:39
 * @apiNote 检查角色注解实现类
 */
@Aspect
@Component
@Slf4j
@Order(2)
public class CheckRoleAspect {

    @Before("execution(* *..controller..*(..))")
    public void before(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckRole annotation = method.getAnnotation(CheckRole.class);

        if (annotation != null) {
            String[] checkRoles = annotation.roles();
            if (checkRoles.length != 0){
                String op = annotation.opt().name();
                final LoginUser loginUser = LoginUserHolder.get();
                final String loginUserId = loginUser.getUser().getId();
                final List<LoginUser.Power> powers = loginUser.getPowers();
                final Set<String> hasRoles = powers.stream().map(LoginUser.Power::getRoleName).collect(Collectors.toSet());
                boolean flag;
                switch (op){
                    case Const.AND: flag = check(checkRoles,hasRoles,true) ;break;
                    case Const.OR:
                    default:flag = check(checkRoles,hasRoles,false) ;break;
                }
                if(!flag){
                    throw new NoAuthorityException(loginUserId,"未授权该角色");
                }
            }
        }
    }

    private boolean check(String[] roles,Set<String> hasRoles,boolean opt){
        List<String> roleList = Arrays.asList(roles);
        if (opt){
            return hasRoles.containsAll(roleList);
        }else {
            for (String hasRole : hasRoles) {
                if (roleList.contains(hasRole)) {
                    return true;
                }
            }
            return false;
        }
    }

    @After("execution(* *..controller..*(..))")
    public void after(){
        LoginUserHolder.remove();
    }
}
