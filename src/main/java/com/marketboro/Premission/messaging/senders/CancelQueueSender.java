package com.marketboro.Premission.messaging.senders;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CancelQueueSender {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue3}")
    private String cancelQueueName;

    @Autowired
    public CancelQueueSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendCancelMessage(Long memberId, String memberName, int pointsToCancel) {
        // cancelQueue로 메시지 발송
        String message = memberId + "" + memberName + "" + pointsToCancel;
        rabbitTemplate.convertAndSend(cancelQueueName, message);
        System.out.println("Sent message to cancelQueue: " + message);
    }
}
