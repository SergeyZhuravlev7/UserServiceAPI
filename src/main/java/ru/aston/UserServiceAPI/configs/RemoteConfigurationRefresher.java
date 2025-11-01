package ru.aston.UserServiceAPI.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.aston.UserServiceAPI.kafka.ProducerService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class RemoteConfigurationRefresher {
    private static final String URL = "http://localhost:9090/config/user-service";
    private static final Logger log = LoggerFactory.getLogger(RemoteConfigurationRefresher.class);
    /*
    Не знаю насколько мне удалось реализовать обновление бинов через scheduler, но вроде
    все работает как надо, для datasource не стал делать, посчитал что маловероятный сценарий,
    хотя и для кафки наверное тоже, это скорее ради любопытства

    Плюс попробовал реализовать чтобы когда начнется обновление кафки для producerService, сначала потоки
    закончили свою работу, потом обновить, и они продолжили писать на новый сервер кафки или в новый топик.
    Дай комментарий потом плез
    */
    private final KafkaAdmin kafkaAdmin;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ConfigurableEnvironment environment;
    private final ConfigurableApplicationContext applicationContext;
    private Properties oldProps = new Properties();

    @Autowired
    RemoteConfigurationRefresher(ConfigurableEnvironment environment,
            ConfigurableApplicationContext applicationContext,
            KafkaAdmin kafkaAdmin) {
        this.environment = environment;
        this.applicationContext = applicationContext;
        this.kafkaAdmin = kafkaAdmin;
    }

    @Scheduled (fixedRate = 5, initialDelay = 10, timeUnit = TimeUnit.MINUTES)
    public void refreshConfig() {
        log.info("Refreshing configuration");
        String config;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(URL,String.class);
            config = response.getBody();
        } catch (Exception e) {
            log.error("Configuration server is unavailable.");
            return;
        }
        updateConfig(config);
    }

    private void updateConfig(String config) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        Resource resources = new ByteArrayResource(config.getBytes());
        yaml.setResources(resources);
        Properties newProps = yaml.getObject();
        if (oldProps.equals(newProps)) return;
        environment
                .getPropertySources()
                .remove("external");
        environment
                .getPropertySources()
                .addFirst(new PropertiesPropertySource("external",newProps));
        registerNewBeans(oldProps,newProps);
        oldProps = newProps;
    }

    private void registerNewBeans(Properties oldProps,Properties newProps) {
        if (oldProps == null || oldProps.isEmpty()) {
            return;
        }
        checkKafkaProps(newProps);
    }

    private void checkKafkaProps(Properties newProps) {
        if (! oldProps
                .getProperty("spring.kafka.bootstrap-servers")
                .equals(newProps.getProperty("spring.kafka.bootstrap-servers"))) registerKafkaBean(applicationContext);
        if (! oldProps
                .getProperty("notifications.topic")
                .equals(newProps.getProperty("notifications.topic"))) registerTopicBean(applicationContext);
    }

    private void registerKafkaBean(ConfigurableApplicationContext applicationContext) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,environment.getProperty("spring.kafka.bootstrap-servers"));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,false);
        DefaultKafkaProducerFactory<String, String> kafkaProducerFactory = new DefaultKafkaProducerFactory<>(config);
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(kafkaProducerFactory);
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        ((DefaultSingletonBeanRegistry) beanFactory).destroySingleton("kafkaTemplate");
        ((DefaultSingletonBeanRegistry) beanFactory).destroySingleton("producerFactory");
        beanFactory.registerSingleton("producerFactory",kafkaProducerFactory);
        beanFactory.registerSingleton("kafkaTemplate",kafkaTemplate);
        ProducerService producerService = applicationContext.getBean(ProducerService.class);
        producerService.setKafkaTemplateOrTopic(kafkaTemplate,null);
    }

    private void registerTopicBean(ConfigurableApplicationContext applicationContext) {
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        if (beanFactory.containsSingleton("newTopic"))
            ((DefaultSingletonBeanRegistry) beanFactory).destroySingleton("newTopic");
        NewTopic topic = TopicBuilder
                .name(environment.getProperty("notifications.topic"))
                .partitions(1)
                .replicas(1)
                .build()
                ;
        kafkaAdmin.createOrModifyTopics(topic);
        ProducerService producerService = applicationContext.getBean(ProducerService.class);
        producerService.setKafkaTemplateOrTopic(null,environment.getProperty("notifications.topic"));
    }
}
