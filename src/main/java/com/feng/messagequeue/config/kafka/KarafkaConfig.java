package com.feng.messagequeue.config.kafka;

import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.feng.messagequeue.common.Constant;
import com.feng.messagequeue.props.KarafkaProps;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class KarafkaConfig {
    final KarafkaProps karafkaProps;

    public KarafkaConfig(KarafkaProps karafkaProps) {
        this.karafkaProps = karafkaProps;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = karafkaProps.getProperties();
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, Constant.ACKS);
        configProps.put(ProducerConfig.RETRIES_CONFIG, Constant.RETRY_NUMBER);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, Constant.RETRY_BACKOFF_MS);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = karafkaProps.getProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constant.GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, Constant.AUTO_COMMIT_INTERVAL_MS);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Constant.EARLIEST);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String>
                factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(Constant.NUM_CONCURRENCY);
        factory.setCommonErrorHandler(errorHandler());
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    private DeadLetterPublishingRecoverer publishingRecover() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate(), (r, e) -> {
            log.error("Exception in publishingRecover : {} ", e.getMessage());
            if (e.getCause() instanceof RecoverableDataAccessException) {
                return new TopicPartition(Constant.RETRY_TOPIC, r.partition());
            } else {
                return new TopicPartition(Constant.DEAD_LETTER_TOPIC, r.partition());
            }
        });
    }

    private DefaultErrorHandler errorHandler() {
        var exceptiopnToIgnorelist = List.of(IllegalArgumentException.class);
        ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(2);
        expBackOff.setInitialInterval(1_000L);
        expBackOff.setMultiplier(2.0);
        expBackOff.setMaxInterval(2_000L);
        var defaultErrorHandler = new DefaultErrorHandler(publishingRecover(), expBackOff);
        exceptiopnToIgnorelist.forEach(defaultErrorHandler::addNotRetryableExceptions);
        defaultErrorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                        log.info("Failed Record in Retry Listener  exception : {} , deliveryAttempt : {} ", ex.getMessage(), deliveryAttempt)
        );
        return defaultErrorHandler;
    }
}
