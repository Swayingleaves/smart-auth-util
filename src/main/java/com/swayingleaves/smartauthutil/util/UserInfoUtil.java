package com.swayingleaves.smartauthutil.util;

import com.swayingleaves.smartauthutil.code.Const;
import com.swayingleaves.smartauthutil.exception.NoAuthorityException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>描述: xxxx </p>
 *
 * @author zhenglin
 * @since 2020/6/24 3:49 下午
 */
public class UserInfoUtil {
    /**
     * 根据token返回登录用户信息
     * @param token 登录密钥
     * @param redisTemplate Redis
     * @return 用户信息
     */
    public static String getUserInfo(String token,RedisTemplate redisTemplate){
        String loginUserTokenKey = AuthRedisKeyUtil.getLoginUserTokenKey(token);
        if (redisTemplate.hasKey(loginUserTokenKey)){
            String userId = (String)redisTemplate.opsForValue().get(loginUserTokenKey);
            if (StringUtils.isNotBlank(userId)){
                String loginUserIdKey = AuthRedisKeyUtil.getLoginUserIdKey(userId);
                if (redisTemplate.hasKey(loginUserIdKey)){
                    return (String) redisTemplate.opsForValue().get(loginUserIdKey);
                }
            }
        }
        return null;
    }

    /**
     * 获取登录用户的权限
     */
    public static Map<String, List<String>> getPower(HttpServletRequest request, Object loginUserId){
        final Object attribute = request.getAttribute(Const.LOGIN_USER_POWER);
        if (attribute == null) {
            throw new NoAuthorityException(loginUserId,"未授权该角色");
        }
        return  (Map<String,List<String>>) attribute;
    }
}
