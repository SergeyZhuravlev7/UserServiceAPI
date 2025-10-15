package ru.aston.UserServiceAPI.kafka;

import org.springframework.kafka.core.KafkaTemplate;

public interface Sendable {

    void send(KafkaTemplate<String, String> kafkaTemplate,String topic);
}
