package ru.aston.UserServiceAPI.configs;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.aston.UserServiceAPI.dtos.ServiceDTO;

import java.util.UUID;

@Component
public class RegistrationConfig {

    private static final Logger log = LoggerFactory.getLogger(RegistrationConfig.class);
    private final String registrationServerURL = "http://localhost:5050/register/user-service";
    private final String deleteServerURL = "http://localhost:5050/delete/user-service/";
    @Value ("${service.url}")
    private String serviceURL;
    private ServiceDTO serviceDTO;

    @PostConstruct
    public void init() throws InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        serviceDTO = new ServiceDTO(UUID
                .randomUUID()
                .toString(),serviceURL);
        try {
            restTemplate.postForEntity(registrationServerURL,serviceDTO,String.class);
        } catch (Exception e) {
            log.error("Registration Service Failed");
            log.info("Try again after 5 seconds");
            Thread.sleep(5000);
            init();
        }
    }

    @PreDestroy
    public void destroy() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.delete(deleteServerURL + serviceDTO.getId());
        } catch (Exception e) {
            log.error("Delete Service Failed");
        }
    }
}
