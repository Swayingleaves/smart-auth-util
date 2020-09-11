package com.swayingleaves.smartauthutil.annotation;

import com.swayingleaves.smartauthutil.code.AuthOpt;

import java.lang.annotation.*;

/**
 * @author : zhenglin
 * @since : 2019/8/26 12:05
 * @apiNote : 检查是否属于该组
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckGroup {
    /**
     * 组名
     */
    String[] group() default {};

    AuthOpt opt() default AuthOpt.AND;
}
