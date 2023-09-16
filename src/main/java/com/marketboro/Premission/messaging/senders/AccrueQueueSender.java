package com.marketboro.Premission.messaging.senders;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccrueQueueSender {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue}")
    private String accrueQueueName;

    @Autowired
    public AccrueQueueSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAccrueMessage(Long memberId, int points) {
        // accrueQueue로 메시지 발송
        String message = memberId + " - " + points;
        rabbitTemplate.convertAndSend(accrueQueueName, message);
        System.out.println("Sent message to accrueQueue: " + message);
    }
}

