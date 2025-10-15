package ru.aston.UserServiceAPI.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;

    @Autowired
    public ProducerService(KafkaTemplate<String, String> kafkaTemplate,@Value ("${notifications.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Async
    public void send(Sendable sendable) {
        sendable.send(kafkaTemplate,topicName);
    }

}
