package com.marketboro.Premission.service;

import com.marketboro.Premission.dto.PointDto;
import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.UseQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Retryable(
        value = { Exception.class },
        maxAttempts = 3, // 최대 재시도 횟수
        backoff = @Backoff(delay = 1000))
@Service
@Transactional(readOnly = true)
public class UsePointServiceImpl implements UsePointService {

    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;

    private final UseQueueSender useQueueSender;


    @Autowired
    public UsePointServiceImpl(MemberRepository memberRepository, HistoryRepository historyRepository, UseQueueSender useQueueSender, MemberServiceImpl memberService) {
        this.memberRepository = memberRepository;
        this.historyRepository = historyRepository;
        this.useQueueSender = useQueueSender;
    }

    @Async
    @Transactional
    public CompletableFuture<PointDto.UsePointResponse> usePointsAsync(Long memberId, String memberName, int pointsToUse) {
        final Optional<Member> optionalMember = Optional.ofNullable(memberRepository.findByMemberId(memberId));
        final Member member = optionalMember.orElseThrow(() -> new MemberException(MemberErrorResult.MEMBER_NOT_FOUND));

        if (member.getMemberName() == null || !member.getMemberName().equals(memberName)) {
            throw new MemberException(MemberErrorResult.NOT_MEMBER_OWNER);
        }

        if (pointsToUse <= 0) {
            throw new MemberException(MemberErrorResult.NEGATIVE_POINTS);
        }

        List<History> historyList = historyRepository.findByMember(member);

        int remainingPointsToUse = pointsToUse;
        int availablePoints = member.getRewardPoints(); // 보유포인트 가져오기

        if (pointsToUse > availablePoints) {
            throw new MemberException(MemberErrorResult.INSUFFICIENT_POINTS); // 보유포인트보다 많이 사용할 때 예외 던지기
        }

        for (History history : historyList) {
            if (remainingPointsToUse <= 0) {
                break;
            }

            availablePoints = history.getPoints();
            if (availablePoints <= 0) {
                continue;
            }

            int pointsToDeduct = Math.min(remainingPointsToUse, availablePoints);

            history.setPoints(availablePoints - pointsToDeduct);
            historyRepository.save(history);

            remainingPointsToUse -= pointsToDeduct;
        }

        try {
            // 비동기적으로 적립 메시지를 RabbitMQ에 전송
            useQueueSender.sendUseMessage(memberId, memberName, pointsToUse);
        } catch (Exception e) {
            // 메세지 전송 중 예외 발생 시, 재시도를 위해 예외를 다시 던짐
            throw new MemberException(MemberErrorResult.FAIL_TO_MESSAGE);
        }

        PointDto.UsePointResponse response = new PointDto.UsePointResponse();
        response.setPointsUsed(pointsToUse);

        return CompletableFuture.completedFuture(response);
    }
}