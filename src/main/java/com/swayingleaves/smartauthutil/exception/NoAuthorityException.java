package com.swayingleaves.smartauthutil.exception;

/**
 * @author zhenglin
 * @since 2019/9/16 14:57
 * @apiNote  无权限异常
 */
public class NoAuthorityException extends RuntimeException {
    public NoAuthorityException() {
    }

    public NoAuthorityException(String message) {
        super(message);
    }

    public NoAuthorityException(Object object,String message) {
        super(object+":"+message);
    }
}
