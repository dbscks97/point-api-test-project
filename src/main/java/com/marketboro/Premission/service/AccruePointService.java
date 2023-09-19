package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.AccrueQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
public class AccruePointService implements PointService {


    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final AccrueQueueSender accrueQueueSender;
    private final MemberService memberService;

    @Autowired
    public AccruePointService(
            HistoryRepository historyRepository,
            MemberRepository memberRepository,
            AccrueQueueSender accrueQueueSender,
            MemberService memberService) {
        this.historyRepository = historyRepository;
        this.memberRepository = memberRepository;
        this.accrueQueueSender = accrueQueueSender;
        this.memberService = memberService;
    }
    @Value("${rabbitmq.queue}")
    private String accrueQueueName;

    @Value("${rabbitmq.queue2}")
    private String useQueueName;

    @Value("${rabbitmq.queue3}")
    private String cancelQueueName;

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

        // 비동기적으로 적립 메시지를 RabbitMQ에 전송
        accrueQueueSender.sendAccrueMessage(memberId, points);

        // CompletableFuture.completedFuture를 사용하여 비동기 작업이 완료되었음을 알립니다.
        return CompletableFuture.completedFuture(null);
    }






}
