package ru.aston.UserServiceAPI.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ProducerService {

    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private KafkaTemplate<String, String> kafkaTemplate;
    private String topicName;
    private AtomicBoolean isUpdated = new AtomicBoolean(false);

    @Autowired
    public ProducerService(KafkaTemplate<String, String> kafkaTemplate,@Value ("${notifications.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Async
    public void send(Sendable sendable) {
        try {
            lock.readLock().lock();
            sendable.send(kafkaTemplate, topicName);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void setKafkaTemplateOrTopic(KafkaTemplate<String, String> kafkaTemplate, String topicName) {
        this.isUpdated.set(true);
        Boolean locked = false;
        try {
            lock.writeLock().lockInterruptibly();
            locked = true;
            if (kafkaTemplate != null) this.kafkaTemplate = kafkaTemplate;
            if (topicName != null) this.topicName = topicName;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for lock");
        } finally {
            this.isUpdated.set(false);
            if (locked) lock.writeLock().unlock();
        }
    }
}
