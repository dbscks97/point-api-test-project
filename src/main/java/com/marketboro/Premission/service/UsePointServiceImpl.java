package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.messaging.senders.UseQueueSender;
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

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
public class UsePointServiceImpl implements UsePointService {

    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;

    private final UseQueueSender useQueueSender;

    @Autowired
    public UsePointServiceImpl(MemberRepository memberRepository, HistoryRepository historyRepository, UseQueueSender useQueueSender) {
        this.memberRepository = memberRepository;
        this.historyRepository = historyRepository;
        this.useQueueSender = useQueueSender;
    }

    @Async
    @Transactional
    public CompletableFuture<Void> usePointsAsync(Long memberId, int pointsToUse) {
        if (pointsToUse <= 0) {
            throw new IllegalArgumentException("사용 포인트는 1 이상이어야 합니다.");
        }

        Member member = memberRepository.findByMemberId(memberId);

        // 해당 회원의 적립금 내역을 가져옵니다 (FIFO 순서로 정렬)
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "historyDate"));
        Page<History> rewardHistoriesPage = historyRepository.findByMemberAndPointsGreaterThan(member, 0, pageable);

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
}
