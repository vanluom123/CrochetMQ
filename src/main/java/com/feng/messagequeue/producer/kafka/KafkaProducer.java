package com.feng.messagequeue.producer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.feng.messagequeue.common.Constant;
import com.feng.messagequeue.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void send(BaseEvent event) throws JsonProcessingException {
        String key = event.getId();
        String value = objectMapper.writeValueAsString(event);
        ProducerRecord<String, String> producerRecord = buildProducerRecord(key, value);

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

    private ProducerRecord<String, String> buildProducerRecord(String key, String value) {
        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(Constant.TOPIC_NAME, null, key, value, recordHeaders);
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
