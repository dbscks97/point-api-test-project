package com.marketboro.Premission.messaging.listeners;

import com.marketboro.Premission.service.AccruePointServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccrueQueueReceiver {
    private final AccruePointServiceImpl accruePointService;

    @Autowired
    public AccrueQueueReceiver(AccruePointServiceImpl accruePointService) {
        this.accruePointService = accruePointService;
    }

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleAccrueMessage(String message) {
        // accrueQueue에서 메시지를 수신하고 처리
        System.out.println("Received message from accrueQueue: " + message);

        // 메시지를 분석하여 적립 포인트를 처리
        String[] parts = message.split("-");
        Long memberId = Long.parseLong(parts[0].trim());
        String memberName = parts[1].trim();
        int points = Integer.parseInt(parts[2].trim());

        // 적립 이벤트 처리
        accruePointService.accruePointsAsync(memberId, memberName,points);
    }
}
