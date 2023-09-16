package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.AccrueQueueSender;
import com.marketboro.Premission.messaging.senders.CancelQueueSender;
import com.marketboro.Premission.messaging.senders.UseQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
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
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<Void> accruePointsAsync(Long memberId, int points) {
//        if (!memberService.isValidMemberId(memberId)) {
//            throw new MemberException("유효하지 않은 회원번호입니다.");
//        }
//
//        if (points <= 0) {
//            throw new MemberException("적립 포인트는 1 이상이어야 합니다.");
//        }

        Member member = memberRepository.findByMemberId(memberId);

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

    @Async
    public CompletableFuture<Void> usePointsAsync(Long memberId, int pointsToUse) {
        if (pointsToUse <= 0) {
            throw new IllegalArgumentException("사용 포인트는 1 이상이어야 합니다.");
        }

        Member member = memberRepository.findByMemberId(memberId);

        // 해당 회원의 적립금 내역을 가져옵니다 (FIFO 순서로 정렬)
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "historyDate"));
        Page<History> rewardHistoriesPage = historyRepository.findByMemberAndPointsGreaterThan(member, 0, pageable);

        // Check if rewardHistoriesPage is null or empty
        if (rewardHistoriesPage == null || rewardHistoriesPage.isEmpty()) {
            // Handle the case where there are no reward histories
            // You can throw an exception or handle it according to your requirements
//            throw new MemberException("No reward histories found for the member.");
        }

        List<History> rewardHistories = rewardHistoriesPage.getContent(); // Page에서 내역 목록을 가져옴

        int remainingPointsToUse = pointsToUse;

        // 적립금 내역을 순회하면서 사용할 포인트 차감
        for (History history : rewardHistories) {
            if (remainingPointsToUse <= 0) {
                break; // 모든 포인트를 사용했으면 종료
            }

            int availablePoints = history.getPoints();
            int pointsUsed = Math.min(availablePoints, remainingPointsToUse); // 현재 내역에서 사용할 포인트

            // 현재 내역에서 사용한 포인트 차감
            history.setPoints(availablePoints - pointsUsed);

            // 남은 포인트 업데이트
            remainingPointsToUse -= pointsUsed;
        }

        // 사용한 적립금 내역을 업데이트
        historyRepository.saveAll(rewardHistories);

        // 사용 내역을 기록
        if (remainingPointsToUse > 0) {
            // 남은 포인트가 있다면 사용 내역으로 기록
            History useHistory = new History();
            useHistory.setMember(member);
            useHistory.setPoints(-remainingPointsToUse); // 사용한 포인트는 음수로 표시
            useHistory.setHistoryDate(new Date());
            historyRepository.save(useHistory);
        }

        // 비동기적으로 사용 메시지를 전송
        useQueueSender.sendUseMessage(memberId, pointsToUse);

        // CompletableFuture.completedFuture를 사용하여 비동기 작업이 완료되었음을 알립니다.
        return CompletableFuture.completedFuture(null);
    }


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
