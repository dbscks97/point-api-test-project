package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.exception.MemberServiceException;
import com.marketboro.Premission.messaging.senders.AccrueQueueSender;
import com.marketboro.Premission.messaging.senders.CancelQueueSender;
import com.marketboro.Premission.messaging.senders.UseQueueSender;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.repository.HistoryRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;


    @Autowired
    public MemberService(
            MemberRepository memberRepository,
            HistoryRepository historyRepository

    ) {
        this.memberRepository = memberRepository;
        this.historyRepository = historyRepository;

    }

    public int getRewardPointsByMemberId(Long memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        if (member != null) {
            return member.getRewardPoints();
        }
        return 0;
    }

    public List<History> getHistoriesByMemberId(Long memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        if (member != null) {
            return historyRepository.findByMemberOrderByHistoryDateDesc(member);
        }
        return Collections.emptyList();
    }

    public boolean isValidMemberId(Long memberId) {
        // 회원 유효성 검증 로직을 구현
        Member member = memberRepository.findByMemberId(memberId);
        return member != null;
    }


}
