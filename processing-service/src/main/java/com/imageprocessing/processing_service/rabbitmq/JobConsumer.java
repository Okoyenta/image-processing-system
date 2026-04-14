package com.imageprocessing.processing_service.rabbitmq;

import com.imageprocessing.processing_service.model.ImageUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class JobConsumer {

    @RabbitListener(queues = "jobs.queue")
    public void process(String job) {
        log.info("[Rabbit]: Received job: {}", job);
    }

}
