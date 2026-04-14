package com.imageprocessing.upload_service.kafka;


import com.imageprocessing.upload_service.model.ImageUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageEventProducer {

    private static final String TOPIC = "image-uploaded";
    private final KafkaTemplate<String, ImageUpload> kafkaTemplate;

    public void publish (ImageUpload data){
        kafkaTemplate.send(TOPIC, data.getId().toString(), data);
        log.info("[Kafka] Published to '{}': imageId={}", TOPIC, data.getId());
    }
}
