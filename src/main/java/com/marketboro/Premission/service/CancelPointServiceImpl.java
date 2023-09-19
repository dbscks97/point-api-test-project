package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
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
    public CompletableFuture<Void> cancelPointsAsync(Long memberId, int pointsToCancel) {
        if (pointsToCancel <= 0) {
            throw new IllegalArgumentException("취소 포인트는 1 이상이어야 합니다.");
        }

        Member member = memberRepository.findByMemberId(memberId);

        // 해당 회원의 적립금 사용 내역을 가져옵니다 (LIFO 순서로 정렬)
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "historyDate"));
        Page<History> redeemHistoriesPage = historyRepository.findByMemberAndPointsLessThan(member, 0, pageable);

        List<History> redeemHistories = redeemHistoriesPage.getContent(); // Page에서 내역 목록을 가져옴

        int remainingPointsToCancel = pointsToCancel;

        // 적립금 사용 내역을 순회하면서 취소할 포인트 복구
        for (History history : redeemHistories) {
            if (remainingPointsToCancel <= 0) {
                break; // 모든 포인트를 취소했으면 종료
            }

            int usedPoints = Math.abs(history.getPoints()); // 사용된 포인트를 양수로 변환
            int pointsToRecover = Math.min(usedPoints, remainingPointsToCancel); // 현재 내역에서 복구할 포인트

            // 현재 내역에서 복구한 포인트 추가
            history.setPoints(-usedPoints + pointsToRecover);

            // 남은 포인트 업데이트
            remainingPointsToCancel -= pointsToRecover;
        }

        // 취소한 적립금 내역을 업데이트
        historyRepository.saveAll(redeemHistories);

        // 비동기적으로 취소 메시지를 전송
        cancelQueueSender.sendCancelMessage(memberId, pointsToCancel);

        // CompletableFuture.completedFuture를 사용하여 비동기 작업이 완료되었음을 알립니다.
        return CompletableFuture.completedFuture(null);
    }
}
