package ru.aston.UserServiceAPI.configs;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

public class RemoteConfigurationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String URL = "http://localhost:9090/config/user-service";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        RestTemplate restTemplate = new RestTemplate();
        String config;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(URL,String.class);
            config = response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load config from config server : " + URL,e);
        }
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        Resource resources = new ByteArrayResource(config.getBytes());
        yaml.setResources(resources);
        Properties properties = yaml.getObject();
        environment
                .getPropertySources()
                .addFirst(new PropertiesPropertySource("external",properties));
    }
}
