package com.swayingleaves.smartauthutil.util;

import com.swayingleaves.smartauthutil.code.Const;

/**
 * @author zhenglin
 * @since 2020/5/13 12:17 下午
 * @apiNote 构件Redis key 相关工具类
 */
public class AuthRedisKeyUtil {

    public static String getLoginUserTokenIdKey(String uid){
        return Const.SU_LOGIN_USER_TOKEN + uid;
    }

    public static String getLoginUserTokenKey(String token){
        return Const.SU_LOGIN_USER_TOKEN + token;
    }

    public static String getLoginUserIpKey(String ip){
        return Const.SU_LOGIN_USER_IP + ip;
    }

    public static String getLoginUserIdKey(String uid){
        return Const.SU_LOGIN_USER_ID + uid;
    }
}
