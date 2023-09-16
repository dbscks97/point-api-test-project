package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.exception.MemberServiceException;
import com.marketboro.Premission.messaging.senders.AccrueQueueSender;
import com.marketboro.Premission.messaging.senders.CancelQueueSender;
import com.marketboro.Premission.messaging.senders.UseQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;

@Service
@Transactional
public class PointService {

    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final AccrueQueueSender accrueQueueSender;
    private final UseQueueSender useQueueSender;
    private final CancelQueueSender cancelQueueSender;
    private final MemberService memberService;

    @Value("${rabbitmq.queue}")
    private String accrueQueueName;

    @Value("${rabbitmq.queue2}")
    private String useQueueName;

    @Value("${rabbitmq.queue3}")
    private String cancelQueueName;
    public PointService(HistoryRepository historyRepository, MemberRepository memberRepository, AccrueQueueSender accrueQueueSender, UseQueueSender useQueueSender, CancelQueueSender cancelQueueSender, MemberService memberService) {
        this.historyRepository = historyRepository;
        this.memberRepository = memberRepository;
        this.accrueQueueSender = accrueQueueSender;
        this.useQueueSender = useQueueSender;
        this.cancelQueueSender = cancelQueueSender;
        this.memberService = memberService;
    }

    @Async
    public void accruePointsAsync(Long memberId, int points) {
        Member member = memberRepository.findByMemberId(memberId);
        if (!memberService.isValidMemberId(memberId)) {
            throw new MemberServiceException("유효하지 않은 회원번호입니다.");
        }

        if (points <= 0) {
            throw new MemberServiceException("적립 포인트는 1 이상이어야 합니다.");
        }

        member.setRewardPoints(member.getRewardPoints() + points);

        History history = new History();
        history.setMember(member);
        history.setPoints(points);
        historyRepository.save(history);

        // 적립금의 유효기간 설정 (적립 후에 설정)
        Calendar calendar = Calendar.getInstance();
        history.setHistoryDate(calendar.getTime());

        // 비동기적으로 적립 메시지를 RabbitMQ에 전송
        accrueQueueSender.sendAccrueMessage(memberId, points);
    }

    public void usePointsAsync(Long memberId, int pointsToUse) {

        useQueueSender.sendUseMessage(memberId, pointsToUse);
    }

    public void cancelPointsAsync(Long memberId, int pointsToCancel) {
        // 취소 요청을 비동기적으로 RabbitMQ에 전송
        cancelQueueSender.sendCancelMessage(memberId, pointsToCancel);
    }
}
