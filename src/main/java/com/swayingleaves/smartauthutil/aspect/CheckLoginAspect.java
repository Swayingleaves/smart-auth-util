package com.swayingleaves.smartauthutil.aspect;

import com.alibaba.fastjson.JSONObject;
import com.swayingleaves.smartauthutil.annotation.CheckLogin;
import com.swayingleaves.smartauthutil.code.Const;
import com.swayingleaves.smartauthutil.exception.IllegalRequestException;
import com.swayingleaves.smartauthutil.exception.LoginException;
import com.swayingleaves.smartauthutil.util.AuthRedisKeyUtil;
import com.swayingleaves.smartauthutil.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
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
 * @since 2020/2/21 5:05 下午
 * @apiNote 检查登录注解实现类
 */
@Aspect
@Component
@Slf4j
@Order(1)
public class CheckLoginAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Before("execution(* *..controller..*(..))")
    public void before(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckLogin annotation = method.getAnnotation(CheckLogin.class);

        if (annotation == null){
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
            if(ssoToken != null){
                String loginUserTokenKey = AuthRedisKeyUtil.getLoginUserTokenKey(ssoToken);
                if (redisTemplate.hasKey(loginUserTokenKey)) {
                    String id = (String)redisTemplate.opsForValue().get(loginUserTokenKey);
                    String loginUserIdKey = AuthRedisKeyUtil.getLoginUserIdKey(id);

                    JSONObject loginUserRedis = JSONObject.parseObject(String.valueOf(redisTemplate.opsForValue().get(loginUserIdKey)));
                    final LoginUser loginUser = loginUserRedis.toJavaObject(LoginUser.class);

                    final LoginUser.User user = loginUser.getUser();
                    final String loginUserId = user.getId();
                    final String loginUserNickName = user.getNickName();
                    //设置用户id
                    request.setAttribute(Const.LOGIN_USER_ID,loginUserId);
                    //设置用户名
                    request.setAttribute(Const.LOGIN_USER_NAME,loginUserNickName);
                    //设置用户角色
                    final List<LoginUser.Power> powers = loginUser.getPowers();
                    Map<String,List<String>> powerMap = new HashMap<>(16);
                    for (LoginUser.Power power : powers) {
                        final String roleName = power.getRoleName();
                        final List<LoginUser.Power.Permission> permissions = power.getPermissions();
                        final List<String> collect = permissions.stream().map(LoginUser.Power.Permission::getPmName).collect(Collectors.toList());
                        powerMap.put(roleName,collect);
                    }
                    request.setAttribute(Const.LOGIN_USER_POWER, powerMap);
                    final List<LoginUser.Group> groups = loginUser.getGroups();
                    final List<String> groupList = groups.stream().map(LoginUser.Group::getGroupName).collect(Collectors.toList());
                    request.setAttribute(Const.LOGIN_USER_GROUP, groupList);
                    //延长失效时间
                    ExecutorService singleThread = Executors.newSingleThreadExecutor();
                    singleThread.execute(()->{
                        redisTemplate.expire(loginUserTokenKey, Const.LOGIN_TIME_OFF, TimeUnit.MINUTES);
                        String loginUserTokenIdKey = AuthRedisKeyUtil.getLoginUserTokenIdKey(id);
                        redisTemplate.expire(loginUserTokenIdKey, Const.LOGIN_TIME_OFF, TimeUnit.MINUTES);
                        if (redisTemplate.hasKey(loginUserIdKey)){
                            redisTemplate.expire(loginUserIdKey, Const.LOGIN_TIME_OFF, TimeUnit.MINUTES);
                        }
                    });
                    singleThread.shutdown();
                }else {
                    throw new LoginException("登录已过期");
                }
            }else {
                throw new IllegalRequestException("非法请求");
            }
        }
    }
}
