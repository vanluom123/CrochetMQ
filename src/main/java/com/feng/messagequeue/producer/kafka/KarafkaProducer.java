package com.feng.messagequeue.producer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.messagequeue.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class KarafkaProducer {
    final KafkaTemplate<String, String> kafkaTemplate;
    final ObjectMapper objectMapper;

    public KarafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(BaseEvent event) throws JsonProcessingException {
        String key = event.getId();
        String value = objectMapper.writeValueAsString(event);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(key, value);
        CompletableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send(producerRecord);
        listenableFuture.handle((result, throwable) -> {
            if (throwable == null) {
                handleSuccess(key, value, result);
            } else {
                handleFailure(throwable);
            }
            return null;
        });
    }

    private void handleSuccess(String key, String value, SendResult<String, String> result) {
        log.info("Message sent successFully for the key : {} and the value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }

    private void handleFailure(Throwable ex) {
        log.error("Error sending the message and the exception is {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }
}
