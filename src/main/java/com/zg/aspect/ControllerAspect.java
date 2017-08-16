package com.zg.aspect;

import com.alibaba.fastjson.JSON;
import com.zg.vo.ResponseCode;
import com.zg.vo.ResponseResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * 实现controller层AOP切面
 * @author guang.zhang
 * @version 2017-02-27
 */
@Aspect
@Service
@ControllerAdvice
public class ControllerAspect {

    private static Logger logger = LoggerFactory.getLogger(ControllerAspect.class);

    @Around("execution(* com.zg.controller.*Controller.*(..))")
    public Object process(ProceedingJoinPoint pjp) throws Throwable {
        //writeLog(pjp);
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

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Object handleException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError.getField() + "-" + fieldError.getDefaultMessage();
        ResponseResult responseResult = new ResponseResult(ResponseCode.PARAM_EXCEPTION,msg);
        return new ResponseEntity<>(responseResult, HttpStatus.OK);
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Object handleException(MissingServletRequestParameterException e) {
        ResponseResult responseResult = new ResponseResult(ResponseCode.PARAM_EXCEPTION,e.getMessage());
        return new ResponseEntity<>(responseResult, HttpStatus.OK);
    }

    private void writeLog(ProceedingJoinPoint pjp){
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String method = signature.getDeclaringTypeName() + "." + signature.getName();
            String param = "";
            Object[] args = pjp.getArgs();
            if (args != null && args.length > 0){
                if (args.length == 1){
                    param = JSON.toJSONString(args[0],true);
                }else {
                    String[] argNames = signature.getParameterNames();
                    for (int i = 0; i < args.length; i++){
                        param += argNames[i] + "=" + JSON.toJSONString(args[i]) + " ";
                    }
                }
            }
            logger.info("our method: " + method + ", " + "param: " + param);
        } catch (Exception e) {
            logger.info("打印日志出错",e);
        }
    }
}
