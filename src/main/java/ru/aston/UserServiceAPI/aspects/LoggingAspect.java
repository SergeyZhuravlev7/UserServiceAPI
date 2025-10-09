package ru.aston.UserServiceAPI.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    //Логгирующий аспект, но тут смысл уже есть)))))

    private final Logger userServiceLogger;

    @Autowired
    LoggingAspect(Logger userServiceLogger) {
        this.userServiceLogger = userServiceLogger;
    }

    @Around (value = "@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        StringBuilder argsWithTypes = new StringBuilder();
        for (Object arg : args) {
            argsWithTypes.append(arg.getClass().getSimpleName()).append(" ").append(arg).append(" , ");
        }
        userServiceLogger.info("Called transactional method: {}(). Method args: {}\nOpen transaction...",methodName,argsWithTypes.substring(0,argsWithTypes.length() - 2));
        Object result = null;
        try {
            result = joinPoint.proceed();
            userServiceLogger.info("Successful method call. Result: {}.\n",result);
        } catch (Throwable ex) {
            userServiceLogger.error("Transactional method execution exception: {}\n",ex.getMessage());
        } finally {
            userServiceLogger.info("Closing transaction...");
        }
        return result;
    }

    @AfterThrowing (value = "@within(ru.aston.UserServiceAPI.Utils.Loggable)")
    public void afterThrowing(JoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        StringBuilder argsWithTypes = new StringBuilder();
        for (Object arg : args) {
            if (arg.getClass().getSimpleName().contains("BindingResult")) continue;
            argsWithTypes.append(arg.getClass().getSimpleName()).append(" ").append(arg).append(".");
        }
        userServiceLogger.error("Method {}() throw exception. Method args: {}",methodName,argsWithTypes.substring(0,argsWithTypes.length() - 1));
    }
}
