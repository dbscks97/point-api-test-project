package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@Transactional
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;

    public HistoryService(HistoryRepository historyRepository, MemberRepository memberRepository) {
        this.historyRepository = historyRepository;
        this.memberRepository = memberRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void scheduleExpirePoints() {
        expirePoints();
    }

    public void expirePoints() {
        Calendar currentCalendar = Calendar.getInstance();

        List<History> expiredHistories = new ArrayList<>();

        // 조회한 기록 중에서 만료된 기록을 찾습니다.
        List<History> allHistories = historyRepository.findAll();
        for (History history : allHistories) {
            Calendar historyCalendar = Calendar.getInstance();
            historyCalendar.setTime(history.getHistoryDate());

            // 현재 날짜보다 1년 이상 지난 적립금을 찾습니다.
            if (currentCalendar.after(historyCalendar)) {
                expiredHistories.add(history);
            }
        }

        // 만료된 적립금을 삭제하고 회원의 적립금 업데이트
        for (History expiredHistory : expiredHistories) {
            Member member = expiredHistory.getMember();
            int expiredPoints = expiredHistory.getPoints();

            // 적립금에서 만료된 포인트를 차감합니다.
            member.setRewardPoints(member.getRewardPoints() - expiredPoints);

            // 데이터베이스에서 만료된 기록을 삭제합니다.
            historyRepository.delete(expiredHistory);
        }
    }

    public Page<History> getPagedUsageHistoryByMemberId(Long memberId, String memberName, Pageable pageable) {
        Member member = memberRepository.findByMemberId(memberId);
        if (member == null) {
            throw new MemberException(MemberErrorResult.MEMBER_NOT_FOUND);
        }

        // 본인 확인 로직 추가
        if (!member.getMemberName().equals(memberName)) {
            throw new MemberException(MemberErrorResult.NOT_MEMBER_OWNER);
        }

        return historyRepository.findByMemberMemberIdAndMemberMemberNameOrderByHistoryDateDesc(memberId, memberName, pageable);
    }

}
