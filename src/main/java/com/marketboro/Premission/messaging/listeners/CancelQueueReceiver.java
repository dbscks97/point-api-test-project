package com.marketboro.Premission.messaging.listeners;

import com.marketboro.Premission.service.CancelPointServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CancelQueueReceiver {
    private CancelPointServiceImpl cancelPointServiceImpl;

    @Autowired
    public CancelQueueReceiver(CancelPointServiceImpl cancelPointServiceImpl) {
        this.cancelPointServiceImpl = cancelPointServiceImpl;
    }

    @RabbitListener(queues = "${rabbitmq.queue3}")
    public void handleCancelMessage(String message) {
        // cancelQueue에서 메시지를 수신하고 처리
        System.out.println("Received message from cancelQueue: " + message);

        // 메시지를 분석하여 취소할 포인트와 deductPointNo를 처리
        String[] parts = message.split("");
        Long memberId = Long.parseLong(parts[0].trim());
        String memberName = parts[1].trim();
        int pointsToCancel = Integer.parseInt(parts[2].trim());
        int deductPointNo = Integer.parseInt(parts[3].trim());

        // 취소 이벤트 처리
        cancelPointServiceImpl.cancelPointsAsync(memberId, memberName, pointsToCancel, deductPointNo);
    }
}

