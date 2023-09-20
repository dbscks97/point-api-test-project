package com.marketboro.Premission;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.service.HistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원별 적립금 적립/사용 내역 조회 API")
public class HistoryServiceImplTest {

    @InjectMocks
    private HistoryServiceImpl historyServiceImpl;

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    public void setUp() {
        // given
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setMemberName("12345");
        testMember.setRewardPoints(100);
    }

    @Test
    public void 포인트적립및사용내역_회원존재하지않음() {
        // given
        Pageable pageable = Pageable.unpaged();
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(null);

        // when
        final MemberException result = assertThrows(MemberException.class, () -> historyServiceImpl.getPagedUsageHistoryByMemberId(testMember.getMemberId(), testMember.getMemberName(), pageable));

        // then
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.MEMBER_NOT_FOUND);
    }

    @Test
    public void 포인트적립및사용내역_본인이아님() {
        // given
        Pageable pageable = Pageable.unpaged();
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);

        // when
        final MemberException result = assertThrows(MemberException.class, () -> historyServiceImpl.getPagedUsageHistoryByMemberId(testMember.getMemberId(), "notowner", pageable));

        // then
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.NOT_MEMBER_OWNER);
    }

    @Test
    public void 포인트적립및사용내역조회성공() {
        // given
        Pageable pageable = Pageable.unpaged();
        List<History> historyList = new ArrayList<>();
        historyList.add(new History());
        historyList.add(new History());

        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);
        when(historyRepository.findByMemberMemberIdAndMemberMemberNameOrderByHistoryDateDesc(testMember.getMemberId(),testMember.getMemberName(), pageable)).thenReturn(new PageImpl<>(historyList));

        // when
        Page<History> result = historyServiceImpl.getPagedUsageHistoryByMemberId(testMember.getMemberId(), testMember.getMemberName(), pageable);

        // then
        verify(memberRepository, times(1)).findByMemberId(testMember.getMemberId());
        verify(historyRepository, times(1)).findByMemberMemberIdAndMemberMemberNameOrderByHistoryDateDesc(testMember.getMemberId(),testMember.getMemberName(), pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }
}