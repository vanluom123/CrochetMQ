package com.feng.messagequeue.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "karafka")
public class KarafkaProps {
    Map<String, Object> properties;
}
