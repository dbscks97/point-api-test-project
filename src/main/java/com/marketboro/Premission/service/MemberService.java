package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.response.MemberResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public MemberResponse getPoints(Long memberId, String memberName) {
        final Optional<Member> optionalMember = Optional.ofNullable(memberRepository.findByMemberId(memberId));
        final Member member = optionalMember.orElseThrow(() -> new MemberException(MemberErrorResult.MEMBER_NOT_FOUND));

        if (!member.getMemberName().equals(memberName)) {
            throw new MemberException(MemberErrorResult.NOT_MEMBER_OWNER);
        }

        return MemberResponse.builder()
                .memberId(member.getMemberId())
                .memberName(member.getMemberName())
                .rewardPoints(member.getRewardPoints())
                .build();

    }


}
