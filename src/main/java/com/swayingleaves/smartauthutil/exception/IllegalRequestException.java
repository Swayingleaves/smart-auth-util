package com.swayingleaves.smartauthutil.exception;

/**
 * @author : zhenglin
 * @since : 2019/8/26 17:42
 * @apiNote : 非法访问自定义异常
 */
public class IllegalRequestException extends RuntimeException {
    public IllegalRequestException() {
        super();
    }

    public IllegalRequestException(String message) {
        super(message);
    }
}
