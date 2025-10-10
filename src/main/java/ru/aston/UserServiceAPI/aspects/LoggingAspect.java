package ru.aston.UserServiceAPI.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    //Логгирующий аспект, но тут смысл уже есть)))))

    @Around (value = "@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget()
                                                         .getClass());
        String methodName = joinPoint.getSignature()
                                     .getName();
        StringBuilder argsWithTypes = new StringBuilder();
        for (Object arg : args) {
            argsWithTypes.append(arg.getClass()
                                    .getSimpleName())
                         .append(" ")
                         .append(arg)
                         .append(" | ");
        }
        logger.info("Called transactional method: {}(). Method args: {}.",methodName,argsWithTypes);
        logger.info("Open transaction...");
        Object result = null;
        try {
            result = joinPoint.proceed();
            logger.info("Successful method call. Result: {}.",result);
        } catch (Throwable ex) {
            logger.error("Transactional method execution exception: {}",ex.getMessage());
        } finally {
            logger.info("Closing transaction...");
        }
        return result;
    }

    @AfterThrowing (value = "@within(ru.aston.UserServiceAPI.Utils.Loggable)")
    public void afterThrowing(JoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature()
                                     .getName();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget()
                                                         .getClass());
        StringBuilder argsWithTypes = new StringBuilder();
        for (Object arg : args) {
            if (arg == null || arg.getClass()
                                  .getSimpleName()
                                  .contains("BindingResult")) continue;
            argsWithTypes.append(arg.getClass()
                                    .getSimpleName())
                         .append(" ")
                         .append(arg)
                         .append(".");
        }
        logger.error("Method {}() throw exception. Method args: {}",methodName,argsWithTypes);
    }
}
