package com.company.searchengine.aop;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.company.searchengine.util.LogUtil;
import com.company.searchengine.util.WebUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final WebUtil webUtil;

    @Around("execution(* com.company.searchengine.controller..*(..))")
    public Object logControllerEndpoints(ProceedingJoinPoint joinPoint) throws Throwable {
        String uri = webUtil.getRequestUri();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        Map<String, Object> params = LogUtil.getParamsAsMap(methodSignature.getParameterNames(), joinPoint.getArgs());

        log.debug("[Request]  | Uri: {} [{}.{}] | Params: {}", v("uri", uri), className, methodName, params);

        final long start = System.currentTimeMillis();
        final Object result = joinPoint.proceed();
        final long elapsedTime = System.currentTimeMillis() - start;

        log.debug("[Response] | Uri: {} [{}.{}] | Elapsed time: {} ms | Result: {}",
                uri, className, methodName, v("elapsed_time", elapsedTime), result);

        return result;
    }

    @Around("execution(* com.company.searchengine.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        Map<String, Object> params = LogUtil.getParamsAsMap(methodSignature.getParameterNames(), joinPoint.getArgs());

        log.debug("[Service] | [{}.{}] | Params: {}", className, methodName, params);

        final long start = System.currentTimeMillis();
        final Object result = joinPoint.proceed();
        final long elapsedTime = System.currentTimeMillis() - start;

        log.debug("[Service] | [{}.{}] | Elapsed time: {} ms | Result: {}",
                className, methodName, v("elapsed_time", elapsedTime), result);

        return result;
    }

}
