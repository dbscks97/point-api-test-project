package com.marketboro.Premission;


import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.response.MemberResponse;
import com.marketboro.Premission.service.HistoryService;
import com.marketboro.Premission.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
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
public class MemberServiceTest {

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @InjectMocks
    private HistoryService historyService;



    private Member testMember;
    private History testHistory;



//     테스트 전 기본 데이터 설정
    @BeforeEach
    public void member() {
        // Given: Mock 데이터 설정
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setMemberName("test");
        testMember.setRewardPoints(100);

        testHistory = new History();
        testHistory.setMember(testMember);
        testHistory.setPoints(50);
    }

    // 양수 적립금이 같을 때 조회 테스트
    @Test
    public void 회원적립금조회일치() {
        // Given: findByMemberId 스텁 설정
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);


        // When: 회원별 적립금 조회 API 호출
        MemberResponse result = memberService.getPoints(testMember.getMemberId(),testMember.getMemberName() );

        // Then: 정상적으로 적립금이 반환되는지 확인
        assertThat(result.getRewardPoints()).isEqualTo(testMember.getRewardPoints());
        assertThat(result.getMemberName()).isEqualTo(testMember.getMemberName());
    }

    // 양수 적립금이 같지 않을 때 조회 테스트
    @Test
    public void 회원적립금조회불일치() {
        // Given
        Member differentRewardMember = new Member();
        differentRewardMember.setMemberId(1L);
        differentRewardMember.setMemberName("test");
        differentRewardMember.setRewardPoints(200);

        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(differentRewardMember);

        // When
        MemberResponse result = memberService.getPoints(testMember.getMemberId(), testMember.getMemberName());

        // Then
        assertNotEquals(testMember.getRewardPoints(), result.getRewardPoints());
        assertThat(result.getMemberName()).isEqualTo(testMember.getMemberName());
    }



    // 양수 적립금이 다를 때 조회 테스트
    @Test
    public void 회원적립금조회실패_본인아님() {
        // Given: findByMemberId 스텁 설정
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);

        // When: 회원별 적립금 조회 API 호출
        MemberException result = assertThrows(MemberException.class, () -> memberService.getPoints(testMember.getMemberId(), "notowner"));

        // Then: 정상적으로 적립금이 반환되는지 확인
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.NOT_MEMBER_OWNER);
    }


    @Test
    public void 적립금만료테스트() {
        // Given: 만료될 적립금 데이터 생성
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2); // 2년 전으로 설정하여 만료된 데이터 생성
        History expiredHistory = new History();
        expiredHistory.setMember(testMember);
        expiredHistory.setPoints(30);
        expiredHistory.setHistoryDate(calendar.getTime());

        List<History> expiredHistories = new ArrayList<>(List.of(expiredHistory));

        when(historyRepository.findAll()).thenReturn(expiredHistories);

        // When: 적립금 만료 처리 실행
        historyService.expirePoints();

        // Then: 만료된 적립금이 삭제되었는지 확인
        verify(historyRepository, times(1)).delete(expiredHistory);

        // And: 회원의 적립금이 올바르게 업데이트되었는지 확인
        assertEquals(70, testMember.getRewardPoints());
    }
    @Test
    public void 적립금만료기간이안됐을때테스트() {
        // Given: 만료되지 않은 적립금 데이터 생성
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        History validHistory = new History();
        validHistory.setMember(testMember);
        validHistory.setPoints(20);
        validHistory.setHistoryDate(calendar.getTime());

        List<History> validHistories = new ArrayList<>(List.of(validHistory));

        when(historyRepository.findAll()).thenReturn(validHistories);

        // When: 적립금 만료 처리 실행
        historyService.expirePoints();

        // Then: 만료되지 않은 적립금이 삭제되지 않았는지 확인
        verify(historyRepository, never()).delete(any());

        // And: 회원의 적립금이 변경되지 않았는지 확인
        assertEquals(100, testMember.getRewardPoints());
    }


}
