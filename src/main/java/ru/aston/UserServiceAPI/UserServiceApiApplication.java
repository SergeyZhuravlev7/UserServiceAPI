package ru.aston.UserServiceAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.aston.UserServiceAPI.configs.RemoteConfigurationInitializer;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableHypermediaSupport (type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
public class UserServiceApiApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UserServiceApiApplication.class);
        app.addInitializers(new RemoteConfigurationInitializer());
        app.run(args);
    }
}
