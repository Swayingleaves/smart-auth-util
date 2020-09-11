package com.swayingleaves.smartauthutil.annotation;

import java.lang.annotation.*;

/**
 * @author : zhenglin
 * @since : 2019/8/26 12:05
 * @apiNote : 检查是否登录
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckLogin {

}
