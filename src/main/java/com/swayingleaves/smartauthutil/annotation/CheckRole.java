package com.swayingleaves.smartauthutil.annotation;

import com.swayingleaves.smartauthutil.code.AuthOpt;

import java.lang.annotation.*;

/**
 * @author zhenglin
 * @since 2019/9/16 14:37
 * @apiNote 检查角色注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckRole {
    /**
     * 指定角色
     */
    String[] roles() default {};

    /**
     * 指定角色间关系
     */
    AuthOpt opt() default AuthOpt.OR;
}
