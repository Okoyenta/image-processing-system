package com.imageprocessing.upload_service.rabbitmq;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class QueueConfig {
    public static final String QUEUE = "jobs.queue";
    public static final String EXCHANGE1 = "app.exchange";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE1);
    }

    @Bean
    public Queue jobQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding binding (Queue jobQueue, DirectExchange exchange){
        return BindingBuilder.bind(jobQueue).to(exchange).with("jobs.routing");
    }

//    If you are consuming these messages in a different microservice, ensure that service also has the Jackson2JsonMessageConverter bean defined, or it will fail to deserialize the message!

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
