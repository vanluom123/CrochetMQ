package com.feng.messagequeue.producer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.messagequeue.event.TestEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KarafkaProducerTest {
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KarafkaProducer karafkaProducer;

    private TestEvent event;

    @BeforeEach
    public void setup() {
        event = TestEvent.builder().id("1").name("test name").age(10).build();
        ObjectMapper objectMapper = new ObjectMapper();
        karafkaProducer = new KarafkaProducer(kafkaTemplate, objectMapper);
    }

    @Test
    public void send_shouldHandleSuccess() throws JsonProcessingException {
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        karafkaProducer.send(event);

        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    public void send_shouldHandleFailure() throws JsonProcessingException {
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Test exception"));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        karafkaProducer.send(event);

        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }
}
