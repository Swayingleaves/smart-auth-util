package com.swayingleaves.smartauthutil.code;

import com.swayingleaves.smartauthutil.aspect.LoginUser;

/**
 * @author zhenglin
 * @date 2021/1/15
 */
public class LoginUserHolder {
    public static final ThreadLocal<LoginUser> THREAD_LOCAL = new InheritableThreadLocal<>();

    public static void set(LoginUser loginUser) {
        THREAD_LOCAL.set(loginUser);
    }

    public static LoginUser get() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
