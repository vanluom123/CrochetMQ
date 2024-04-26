package com.feng.messagequeue.config.rabbitmq;

import com.feng.messagequeue.common.Constant;
import com.feng.messagequeue.props.RabbitProps;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQConfig {
    final RabbitProps rabbitProps;

    public RabbitMQConfig(RabbitProps rabbitProps) {
        this.rabbitProps = rabbitProps;
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(Constant.DEAD_LETTER_EXCHANGE_NAME);
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(Constant.EXCHANGE_NAME);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(Constant.DEAD_LETTER_QUEUE_NAME)
                .build();
    }

    @Bean
    Queue queue() {
        return QueueBuilder.durable(Constant.QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", Constant.DEAD_LETTER_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", Constant.DEAD_LETTER_ROUTING_KEY)
                .withArgument("x-message-ttl", rabbitProps.getTtl())
                .build();
    }

    @Bean
    Binding DLQbinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(Constant.DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(Constant.ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setUsername(rabbitProps.getUsername());
        cf.setPassword(rabbitProps.getPassword());
        cf.setUri(rabbitProps.getUri());
        cf.setHost(rabbitProps.getHost());
        cf.setPort(rabbitProps.getPort());
        cf.setChannelCheckoutTimeout(rabbitProps.getChanelCheckoutTimeoutMs());
        cf.setConnectionTimeout(rabbitProps.getConnectionTimeoutMs());
        return cf;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(connectionFactory);
        return containerFactory;
    }

    @Bean("amqpTemplate")
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RetryTemplate retryTemplate = new RetryTemplate();
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(rabbitProps.getRetryMaxAttempts());
        retryTemplate.setRetryPolicy(retryPolicy);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(rabbitProps.getBackOffPeriod());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setRetryTemplate(retryTemplate);
        return rabbitTemplate;
    }
}
