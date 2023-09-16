package com.marketboro.Premission.messaging.listeners;

import com.marketboro.Premission.service.MemberService;
import com.marketboro.Premission.service.PointService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UseQueueReceiver {
    private final PointService pointService;

    @Autowired
    public UseQueueReceiver(PointService pointService) {
        this.pointService = pointService;
    }

    @RabbitListener(queues = "${rabbitmq.queue2}")
    public void handleUseMessage(String message) {
        // useQueue에서 메시지를 수신하고 처리
        System.out.println("Received message from useQueue: " + message);

        // 메시지를 분석하여 사용 포인트를 처리
        String[] parts = message.split("-");
        Long memberId = Long.parseLong(parts[0].trim());
        int pointsToUse = Integer.parseInt(parts[1].trim());

        // 사용 이벤트 처리
        pointService.usePointsAsync(memberId, pointsToUse);
    }
}