package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.CancelQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
public class CancelPointServiceImpl implements CancelPointService{

    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;
    private final CancelQueueSender cancelQueueSender;

    @Autowired
    public CancelPointServiceImpl(MemberRepository memberRepository, HistoryRepository historyRepository, CancelQueueSender cancelQueueSender) {
        this.memberRepository = memberRepository;
        this.historyRepository = historyRepository;
        this.cancelQueueSender = cancelQueueSender;
    }

    @Transactional
    @Async
    public CompletableFuture<Void> cancelPointsAsync(Long memberId, String memberName, int pointsToCancel, int deductPointNo) {
        final Optional<Member> optionalMember = Optional.ofNullable(memberRepository.findByMemberId(memberId));
        final Member member = optionalMember.orElseThrow(() -> new MemberException(MemberErrorResult.MEMBER_NOT_FOUND));

        if (member.getMemberName() == null || !member.getMemberName().equals(memberName)) {
            throw new MemberException(MemberErrorResult.NOT_MEMBER_OWNER);
        }
        if (pointsToCancel <= 0) {
            throw new MemberException(MemberErrorResult.NEGATIVE_POINTS);
        }

        List<History> historyList = historyRepository.findByMemberAndTypeAndDeductPointNo(member, "use", deductPointNo);

        int remainingPointsToCancel = pointsToCancel;
        int totalPointsCanceled = 0;

        for (History history : historyList) {
            if (remainingPointsToCancel <= 0) {
                break;
            }

            int availablePoints = history.getPoints();
            int pointsToDeduct = Math.min(remainingPointsToCancel, availablePoints);

            // 추가: 포인트 잔액이 부족한 경우 예외를 던짐
            if (member.getRewardPoints() < pointsToDeduct) {
                throw new MemberException(MemberErrorResult.INSUFFICIENT_POINTS);
            }

            member.setRewardPoints(member.getRewardPoints() - pointsToDeduct);
            history.setPoints(availablePoints - pointsToDeduct);
            historyRepository.save(history);

            remainingPointsToCancel -= pointsToDeduct;
            totalPointsCanceled += pointsToDeduct;
        }

        if (totalPointsCanceled > 0) {
            member.setRewardPoints(member.getRewardPoints() + totalPointsCanceled);
            memberRepository.save(member);

            cancelQueueSender.sendCancelMessage(memberId, totalPointsCanceled);
        }

        return CompletableFuture.completedFuture(null);
    }
}
