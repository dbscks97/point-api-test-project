package com.marketboro.Premission.application;


import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.response.MemberResponse;
import com.marketboro.Premission.service.HistoryServiceImpl;
import com.marketboro.Premission.service.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
@DisplayName("회원별 적립금 조회 API")
public class MemberServiceImplTest {

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberServiceImpl;

    @InjectMocks
    private HistoryServiceImpl historyServiceImpl;



    private Member testMember;



    @BeforeEach
    public void member() {
        // given
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setMemberName("test");
        testMember.setRewardPoints(100);

    }


    @Test
    public void 회원적립금조회일치() {
        // given
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);

        // when
        int result = memberServiceImpl.getPoints(testMember.getMemberId(), testMember.getMemberName()).getPoints();

        // then
        assertThat(result).isEqualTo(testMember.getRewardPoints());
    }


    @Test
    public void 회원적립금조회불일치() {
        // given
        Member differentRewardMember = new Member();
        differentRewardMember.setMemberId(1L);
        differentRewardMember.setMemberName("test");
        differentRewardMember.setRewardPoints(200);

        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(differentRewardMember);

        // when
        int result = memberServiceImpl.getPoints(testMember.getMemberId(), testMember.getMemberName()).getPoints();

        // then
        assertNotEquals(testMember.getRewardPoints(), result);
    }



    @Test
    public void 회원적립금조회실패_본인아님() {
        // given
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);

        // when
        MemberException result = assertThrows(MemberException.class, () -> memberServiceImpl.getPoints(testMember.getMemberId(), "notowner"));

        // then
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.NOT_MEMBER_OWNER);
    }


    @Test
    public void 적립금만료테스트() {
        // given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2);
        History expiredHistory = new History();
        expiredHistory.setMember(testMember);
        expiredHistory.setPoints(30);
        expiredHistory.setHistoryDate(calendar.getTime());

        List<History> expiredHistories = new ArrayList<>(List.of(expiredHistory));

        when(historyRepository.findAll()).thenReturn(expiredHistories);

        // when
        historyServiceImpl.expirePoints();

        // then
        verify(historyRepository, times(1)).delete(expiredHistory);
        assertEquals(70, testMember.getRewardPoints());
    }
    @Test
    public void 적립금만료기간이안됐을때테스트() {
        // given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        History validHistory = new History();
        validHistory.setMember(testMember);
        validHistory.setPoints(20);
        validHistory.setHistoryDate(calendar.getTime());

        List<History> validHistories = new ArrayList<>(List.of(validHistory));

        when(historyRepository.findAll()).thenReturn(validHistories);

        // when
        historyServiceImpl.expirePoints();

        // then
        verify(historyRepository, never()).delete(any());
        assertEquals(100, testMember.getRewardPoints());
    }


}
