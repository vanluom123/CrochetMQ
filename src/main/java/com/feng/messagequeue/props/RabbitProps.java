package com.feng.messagequeue.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitProps {
    String host;
    String password;
    String username;
    String uri;
    int port;
    int ttl;
    long chanelCheckoutTimeoutMs;
    int connectionTimeoutMs;
    long backOffPeriod;
    int retryMaxAttempts;

}
