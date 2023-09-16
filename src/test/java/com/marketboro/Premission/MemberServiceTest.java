package com.marketboro.Premission;


import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.CodeEnum;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.service.HistoryService;
import com.marketboro.Premission.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


import static org.assertj.core.api.Assertions.as;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(classes = PremissionApplication.class)
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private HistoryService historyService;

    @MockBean
    private HistoryRepository historyRepository;

    @MockBean
    private MemberRepository memberRepository;

    private Member testMember;
    private History testHistory;



//     테스트 전 기본 데이터 설정
    @BeforeEach
    public void setUp() {
        // Given: Mock 데이터 설정
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setRewardPoints(100);

        testHistory = new History();
        testHistory.setMember(testMember);
        testHistory.setPoints(50);

        when(memberRepository.findByMemberId(1L))
                .thenReturn(testMember);
        when(historyRepository.findByMemberOrderByHistoryDateDesc(testMember))
                .thenReturn(new ArrayList<>(List.of(testHistory)));
    }

    // 양수 적립금이 같을 때 조회 테스트
    @Test
    public void testGetRewardPointsEqual() {
        // When: 회원별 적립금 조회 API 호출
        int rewardPoints = memberService.getRewardPointsByMemberId(1L);

        // Then: 정상적으로 적립금이 반환되는지 확인
        assertEquals(100, rewardPoints);
    }


    // 양수 적립금이 다를 때 조회 테스트
    @Test
    public void testGetRewardPointsNotEqual() {
        // When: 회원별 적립금 조회 API 호출
        int rewardPoints = memberService.getRewardPointsByMemberId(1L);

        // Then: 정상적으로 적립금이 반환되는지 확인
        assertNotEquals(50, rewardPoints); // 예상한 값(50)과 실제 값(rewardPoints)이 다를 때 확인
    }

    // 회원이 존재하지 않는 경우에 대한 예외 처리 테스트
    @Test
    public void testGetRewardPointsNonExistentMember() {
        // Given: 회원이 존재하지 않는 경우
        doReturn(null).when(memberRepository).findByMemberId(2L);

        // When: 존재하지 않는 회원의 적립금 조회 API 호출
        // Then: MemberServiceException 예외가 발생해야 함
        final MemberException exception = assertThrows(MemberException.class, () -> memberService.getRewardPointsByMemberId(2L));

        // And: 예외 메시지와 HTTP 상태 코드가 올바른지 확인
        assertThat(exception.getErrorResult()).isEqualTo(MemberErrorResult.NOT_MEMBER);
    }




    @Test
    public void testExpirePoints() {
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
}
