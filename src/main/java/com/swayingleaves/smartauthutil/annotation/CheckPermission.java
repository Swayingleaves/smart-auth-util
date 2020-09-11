package com.swayingleaves.smartauthutil.annotation;

import com.swayingleaves.smartauthutil.code.AuthOpt;

import java.lang.annotation.*;

/**
 * @author zhenglin
 * @since 2019/9/16 14:37
 * @apiNote 检查权限注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckPermission {
    /**
     * 指定权限
     */
    String[] permissions() default {};
    /**
     * 指定权限间关系
     */
    AuthOpt opt() default AuthOpt.OR;
}
