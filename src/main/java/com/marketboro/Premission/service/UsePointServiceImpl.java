package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.UseQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
public class UsePointServiceImpl implements UsePointService {

    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;

    private final UseQueueSender useQueueSender;
    private final MemberServiceImpl memberService;


    @Autowired
    public UsePointServiceImpl(MemberRepository memberRepository, HistoryRepository historyRepository, UseQueueSender useQueueSender, MemberServiceImpl memberService) {
        this.memberRepository = memberRepository;
        this.historyRepository = historyRepository;
        this.useQueueSender = useQueueSender;
        this.memberService = memberService;
    }

    @Async
    @Transactional
    public CompletableFuture<Void> usePointsAsync(Long memberId, String memberName, int pointsToUse) {
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

        useQueueSender.sendUseMessage(memberId, pointsToUse);

        return CompletableFuture.completedFuture(null);
    }
}