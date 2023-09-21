package com.marketboro.Premission.messaging.listeners;

import com.marketboro.Premission.service.UsePointServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UseQueueReceiver {
    private final UsePointServiceImpl usePointServiceImpl;

    @Autowired
    public UseQueueReceiver(UsePointServiceImpl usePointServiceImpl) {
        this.usePointServiceImpl = usePointServiceImpl;
    }

    @RabbitListener(queues = "${rabbitmq.queue2}")
    public void handleUseMessage(String message) {
        // useQueue에서 메시지를 수신하고 처리
        System.out.println("Received message from useQueue: " + message);

        // 메시지를 분석하여 사용 포인트를 처리
        String[] parts = message.split("");
        Long memberId = Long.parseLong(parts[0].trim());
        String memberName = parts[1].trim();
        int pointsToUse = Integer.parseInt(parts[2].trim());

        // 사용 이벤트 처리
        usePointServiceImpl.usePointsAsync(memberId, memberName, pointsToUse);
    }
}