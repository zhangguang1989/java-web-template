package com.zg.aspect;

import com.zg.vo.ResponseResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


/**
 * 实现controller层AOP切面
 * @author guang.zhang
 * @version 2017-02-27
 */
@Aspect
@Service
public class ControllerAspect {

    @Around("execution(* com.zg.controller.*Controller.*(..))")
    public Object process(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            Object obj = pjp.proceed();
            if (obj instanceof ResponseResult){
                result = obj;
            } else {
                result = new ResponseResult(obj);
            }
        } catch (Exception e){
            result = new ResponseResult(e.getMessage());
        }
        return result;
    }
}
