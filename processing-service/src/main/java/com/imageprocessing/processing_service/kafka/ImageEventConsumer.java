package com.imageprocessing.processing_service.kafka;


import com.imageprocessing.processing_service.model.ImageUpload;
import com.imageprocessing.processing_service.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ImageEventConsumer {

    private final ImageProcessingService imageProcessingService; // not the Impl

    @KafkaListener(
            topics = "image-uploaded",
            groupId = "processing-service-group"
    )
    private void onImageUploaded(ImageUpload event) {
        log.info("Received image upload event: {}", event);
        try {
            imageProcessingService.process(event);
            log.info("Finished processing image: {}", event.getId());
        } catch (Exception e) {
            log.error("Error processing image {}: {}", event.getId(), e.getMessage());
        }
    }


}
