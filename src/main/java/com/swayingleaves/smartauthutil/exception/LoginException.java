package com.swayingleaves.smartauthutil.exception;

/**
 * @author : zhenglin
 * @since : 2019/8/26 17:38
 * @apiNote : 登录异常
 */
public class LoginException extends RuntimeException {
    public LoginException() {
        super();
    }

    public LoginException(String message) {
        super(message);
    }
}
