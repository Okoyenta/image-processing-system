package com.imageprocessing.upload_service.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Slf4j
@Component
@Service
public class JobProducer {

    private final RabbitTemplate rabbitTemplate;

    public JobProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendJob(String message) {
        rabbitTemplate.convertAndSend(QueueConfig.EXCHANGE1, "jobs.routing", message);
        log.info("[RabbitMQ] Sent to '{}': {}", QueueConfig.EXCHANGE1, message);
    }
}
