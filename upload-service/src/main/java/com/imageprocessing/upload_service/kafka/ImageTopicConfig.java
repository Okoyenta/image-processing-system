package com.imageprocessing.upload_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration // missing this
public class ImageTopicConfig {

    private final String TOPIC_1 = "image-uploaded";
    private final String TOPIC_2 = "image-completed";
    private final String TOPIC_3 = "image-failed";

    @Bean
    public NewTopic imageUploadedTopic() { // unique method names
        return TopicBuilder.name(TOPIC_1).build();
    }

    @Bean
    public NewTopic imageCompletedTopic() {
        return TopicBuilder.name(TOPIC_2).build();
    }

    @Bean
    public NewTopic imageFailedTopic() {
        return TopicBuilder.name(TOPIC_3).build();
    }
}