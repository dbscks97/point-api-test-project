package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.AccrueQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
public class AccruePointServiceImpl implements AccruePointService {


    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final AccrueQueueSender accrueQueueSender;
    private final MemberServiceImpl memberServiceImpl;

    @Autowired
    public AccruePointServiceImpl(
            HistoryRepository historyRepository,
            MemberRepository memberRepository,
            AccrueQueueSender accrueQueueSender,
            MemberServiceImpl memberServiceImpl) {
        this.historyRepository = historyRepository;
        this.memberRepository = memberRepository;
        this.accrueQueueSender = accrueQueueSender;
        this.memberServiceImpl = memberServiceImpl;
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 3, // 최대 재시도 횟수
            backoff = @Backoff(delay = 1000))
    @Async
    @Transactional
    public CompletableFuture<Void> accruePointsAsync(Long memberId, String memberName,int points) {
        final Optional<Member> optionalMember = Optional.ofNullable(memberRepository.findByMemberId(memberId));
        final Member member = optionalMember.orElseThrow(() -> new MemberException(MemberErrorResult.MEMBER_NOT_FOUND));

        if (member.getMemberName() == null || !member.getMemberName().equals(memberName)) {
            throw new MemberException(MemberErrorResult.NOT_MEMBER_OWNER);
        }
        if (points <= 0) {
            throw new MemberException(MemberErrorResult.NEGATIVE_POINTS);
        }
        // 적립금의 유효기간 설정 (적립 전에 설정)
        Calendar calendar = Calendar.getInstance();
        Date accrueDate = calendar.getTime();

        member.setRewardPoints(member.getRewardPoints() + points);

        History history = new History();
        history.setMember(member);
        history.setPoints(points);
        history.setHistoryDate(accrueDate);
        historyRepository.save(history);

        try {
            // 비동기적으로 적립 메시지를 RabbitMQ에 전송
            accrueQueueSender.sendAccrueMessage(memberId, points);
        } catch (Exception e) {
            // 메세지 전송 중 예외 발생 시, 재시도를 위해 예외를 다시 던짐
            throw new MemberException(MemberErrorResult.FAIL_TO_MESSAGE);
        }
        // CompletableFuture.completedFuture를 사용하여 비동기 작업이 완료되었음을 알립니다.
        return CompletableFuture.completedFuture(null);
    }

}
