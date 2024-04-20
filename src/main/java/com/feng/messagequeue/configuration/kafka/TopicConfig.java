package com.feng.messagequeue.configuration.kafka;

import com.feng.messagequeue.common.Constant;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicConfig {
    @Bean
    public NewTopic topic(){
        return TopicBuilder.name(Constant.TOPIC_NAME)
                .partitions(Constant.PARTITION_NUMBER)
                .replicas(Constant.REPLICAS_NUMBER)
                .build();
    }
}
