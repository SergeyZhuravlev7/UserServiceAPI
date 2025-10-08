package ru.aston.UserServiceAPI;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES).registerModule(new JavaTimeModule());
    }
}
