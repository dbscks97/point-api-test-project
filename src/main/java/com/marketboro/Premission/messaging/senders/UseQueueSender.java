package com.marketboro.Premission.messaging.senders;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UseQueueSender {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue2}")
    private String useQueueName;

    @Autowired
    public UseQueueSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendUseMessage(Long memberId, int pointsToUse) {
        // useQueue로 메시지 발송
        String message = memberId + " - " + pointsToUse;
        rabbitTemplate.convertAndSend(useQueueName, message);
        System.out.println("Sent message to useQueue: " + message);
    }
}

