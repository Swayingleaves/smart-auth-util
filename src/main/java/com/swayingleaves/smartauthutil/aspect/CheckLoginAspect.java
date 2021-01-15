package com.swayingleaves.smartauthutil.aspect;

import com.alibaba.fastjson.JSONObject;
import com.swayingleaves.smartauthutil.annotation.CheckLogin;
import com.swayingleaves.smartauthutil.code.Const;
import com.swayingleaves.smartauthutil.code.LoginUserHolder;
import com.swayingleaves.smartauthutil.exception.IllegalRequestException;
import com.swayingleaves.smartauthutil.exception.LoginException;
import com.swayingleaves.smartauthutil.util.AuthRedisKeyUtil;
import com.swayingleaves.smartauthutil.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author zhenglin
 * @apiNote 检查登录注解实现类
 * @since 2020/2/21 5:05 下午
 */
@Aspect
@Component
@Slf4j
@Order(1)
public class CheckLoginAspect {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Before("execution(* *..controller..*(..))")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckLogin annotation = method.getAnnotation(CheckLogin.class);

        if (annotation == null) {
            //获取类上注解
            annotation = joinPoint.getTarget().getClass().getAnnotation(CheckLogin.class);
        }
        if (annotation != null) {
            //获取到请求的属性
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            //获取到请求对象
            HttpServletRequest request = attributes.getRequest();
            String ssoToken = HttpUtil.getSsoToken(request);
            if (ssoToken != null) {
                String loginUserTokenKey = AuthRedisKeyUtil.getLoginUserTokenKey(ssoToken);
                if (redisTemplate.hasKey(loginUserTokenKey)) {
                    String id = redisTemplate.opsForValue().get(loginUserTokenKey);
                    String loginUserIdKey = AuthRedisKeyUtil.getLoginUserIdKey(id);

                    JSONObject loginUserRedis = JSONObject.parseObject(String.valueOf(redisTemplate.opsForValue().get(loginUserIdKey)));
                    final LoginUser loginUser = loginUserRedis.toJavaObject(LoginUser.class);
                    //添加到threadLocal
                    LoginUserHolder.set(loginUser);
                    //延长失效时间
                    ExecutorService singleThread = Executors.newSingleThreadExecutor();
                    singleThread.execute(() -> {
                        redisTemplate.expire(loginUserTokenKey, Const.LOGIN_TIME_OFF, TimeUnit.MINUTES);
                        String loginUserTokenIdKey = AuthRedisKeyUtil.getLoginUserTokenIdKey(id);
                        redisTemplate.expire(loginUserTokenIdKey, Const.LOGIN_TIME_OFF, TimeUnit.MINUTES);
                        if (redisTemplate.hasKey(loginUserIdKey)) {
                            redisTemplate.expire(loginUserIdKey, Const.LOGIN_TIME_OFF, TimeUnit.MINUTES);
                        }
                    });
                    singleThread.shutdown();
                } else {
                    throw new LoginException("登录已过期");
                }
            } else {
                throw new IllegalRequestException("非法请求");
            }
        }
    }

    @After("execution(* *..controller..*(..))")
    public void after(){
        LoginUserHolder.remove();
    }
}
