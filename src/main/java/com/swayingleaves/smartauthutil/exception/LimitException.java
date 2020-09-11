package com.swayingleaves.smartauthutil.exception;

/**
 * @author : zhenglin
 * @since : 2019/8/20 14:45
 * @apiNote : 自定义限流异常
 */
public class LimitException extends RuntimeException {

    public LimitException() {
        super();
    }

    public LimitException(String message) {
        super(message);
    }
}
