package ru.aston.UserServiceAPI.kafka;

import org.springframework.kafka.core.KafkaTemplate;

public class DeletedKafkaMessage extends AbstractKafkaMessage {

    public DeletedKafkaMessage(String key) {
        super(key,"deleted");
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
    public void send(KafkaTemplate<String, String> kafkaTemplate,String topic) {
        kafkaTemplate.send(topic,this.getKey(),this.getMessage());
    }
}
