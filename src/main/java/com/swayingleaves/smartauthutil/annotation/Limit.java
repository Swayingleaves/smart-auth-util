package com.swayingleaves.smartauthutil.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author : zhenglin
 * @since : 2019/8/20 11:43
 * @apiNote : 限制接口请求次数
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Limit {
    /**
     * 速率 如限制1000次
     */
    int rate() default 1000;

    /**
     * 方法类型
     * @return
     */
    String methodType() default "default-method-type";

    /**
     * 时间 如 TimeUnit.MINUTES 为分 则限制为 每分钟 1000次访问
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

}

