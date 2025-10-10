package ru.aston.UserServiceAPI.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.aston.UserServiceAPI.dtos.UserDTOOut;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.repos.UserRepository;

@Component
@Aspect
public class UpdatingAspect {

    //Бессмысленный с точки зрения логики аспект
    //Сделан просто ради того чтобы лучше понять аспекты

    private final UserRepository userRepository;

    @Autowired
    public UpdatingAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Around (value = "@annotation(ru.aston.UserServiceAPI.Utils.Updatable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget()
                                                         .getClass());
        if (result instanceof UserDTOOut) {
            long id = ((UserDTOOut) result).getId();
            User user = userRepository.getReferenceById(id);
            if (user.getUpdated_at() == null) {
                user.setUpdated_at(user.getCreated_at());
                userRepository.save(user);
                logger.info("Successfully updated User with id {}",id);
            }
        }
        return result;
    }
}
