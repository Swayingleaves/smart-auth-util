package com.swayingleaves.smartauthutil.aspect;

import com.swayingleaves.smartauthutil.annotation.Limit;
import com.swayingleaves.smartauthutil.code.Const;
import com.swayingleaves.smartauthutil.exception.LimitException;
import com.swayingleaves.smartauthutil.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenglin
 * @apiNote 限流注解实现类
 */
@Aspect
@Component
@Slf4j
public class LimitAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Before("execution(* *..controller..*(..))")
    public void doBefore(JoinPoint joinPoint) throws LimitException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Limit annotation = method.getAnnotation(Limit.class);
        if (annotation == null){
            //获取类上注解
            annotation = joinPoint.getTarget().getClass().getAnnotation(Limit.class);
        }
        if (annotation != null) {
            //获取到请求的属性
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            //获取到请求对象
            HttpServletRequest request = attributes.getRequest();

            //获取到请求的属性
            String ip = HttpUtil.getIpAddress(request).replace(".","-");
            StringBuffer requestUrl = request.getRequestURL();


            int rate = annotation.rate();
            String methodType = annotation.methodType()+":";
            TimeUnit timeUnit = annotation.timeUnit();
            //打印方法限制信息
            log.info("method limit rate[{}--{}--{}]",methodType,timeUnit.toString(),rate);

            long count = redisTemplate.opsForValue().increment(Const.IP_LIMIT_DIR_NAME+methodType+ip, 1);
            if (count == 1) {
                redisTemplate.expire(Const.IP_LIMIT_DIR_NAME+methodType+ip,1,timeUnit);
            }

            if(count > rate){
                log.info("用户IP[" + ip + "]Method:["+methodType+"]访问地址[" + requestUrl + "]超过了限定的次数[" + rate + "]");
                throw new LimitException("速率超过限制");
            }
        }
    }
}
