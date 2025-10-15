package ru.aston.UserServiceAPI.kafka;

import org.springframework.kafka.core.KafkaTemplate;

public class CreatedKafkaMessage extends AbstractKafkaMessage {

    public CreatedKafkaMessage(String key) {
        super(key,"created");
    }

    @Override
    public String getKey() {
        return super.getKey();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public void send(KafkaTemplate<String, String> kafkaTemplate,String topicName) {
        kafkaTemplate.send(topicName,this.getKey(),this.getMessage());
    }
}
