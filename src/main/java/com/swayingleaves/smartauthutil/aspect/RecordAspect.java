package com.swayingleaves.smartauthutil.aspect;

import com.swayingleaves.smartauthutil.annotation.Record;
import com.swayingleaves.smartauthutil.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author zhenglin
 * @since 2019/4/22 16:27
 * @apiNote 记录
 */
@Aspect
@Component
@Slf4j
public class RecordAspect {

    @Before("execution(* *..controller..*(..))")
    public void doBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Record annotation = method.getAnnotation(Record.class);
        if (annotation != null){
            //获取到请求的属性
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            //获取到请求对象
            HttpServletRequest request = attributes.getRequest();
            String ip = HttpUtil.getIpAddress(request).replace(".","-");
            //打印信息
            log.info("ip:[{}],url:[{}],method:[{}],class:[{}],method name:[{}],params:[{}]",
                    ip,request.getRequestURL(),request.getMethod()
                    ,joinPoint.getSignature().getDeclaringTypeName(),joinPoint.getSignature().getName()
                    , Arrays.toString(joinPoint.getArgs()));
        }
    }
}
