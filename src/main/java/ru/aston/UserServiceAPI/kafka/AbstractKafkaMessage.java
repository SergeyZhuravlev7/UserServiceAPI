package ru.aston.UserServiceAPI.kafka;

public abstract class AbstractKafkaMessage implements Sendable {

    private final String key;
    private final String message;

    public AbstractKafkaMessage(String key,String message) {
        this.key = key;
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }
}
