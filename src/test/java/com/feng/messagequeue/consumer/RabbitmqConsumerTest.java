package com.feng.messagequeue.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.messagequeue.event.TestEvent;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RabbitmqConsumerTest {

    @Mock
    private Channel channel;

    private RabbitmqConsumer rabbitmqConsumer;

    @BeforeEach
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        rabbitmqConsumer = new RabbitmqConsumer(objectMapper, channel);
    }

    @Test
    public void consume_shouldPutObjectInQueue() throws IOException {
        String queueName = "queueName";
        Class<TestEvent> classType = TestEvent.class;
        String message = "{\n" +
                "  \"id\": \"1\",\n" +
                "  \"name\": \"test name\",\n" +
                "  \"age\": 10\n" +
                "}";
        TestEvent testEvent = new TestEvent().setId("1").setName("test name").setAge(10);

        Delivery delivery = mock(Delivery.class);
        when(delivery.getBody()).thenReturn(message.getBytes());

        doAnswer(invocation -> {
            DeliverCallback deliverCallback = invocation.getArgument(2);
            deliverCallback.handle("", delivery);
            return null;
        }).when(channel).basicConsume(eq(queueName), eq(true), any(DeliverCallback.class), any(CancelCallback.class));

        var obj = rabbitmqConsumer.consume(queueName, classType);
        Assertions.assertEquals(testEvent.getId(), obj.getId());
        Assertions.assertEquals(testEvent.getName(), obj.getName());
        Assertions.assertEquals(testEvent.getAge(), obj.getAge());
    }
}
