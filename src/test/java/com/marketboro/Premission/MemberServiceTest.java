package com.marketboro.Premission;

import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.exception.MemberServiceException;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.service.HistoryService;
import com.marketboro.Premission.service.MemberService;
import com.marketboro.Premission.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = PremissionApplication.class)
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PointService pointService;

    @Autowired
    private HistoryService historyService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private HistoryRepository historyRepository;

    private Member testMember;
    private History testHistory;

    // 테스트 전 기본 데이터 설정
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

    // 양수 포인트 적립 테스트
    @Test
    public void testAccruePoints() {
        // When: 적립 포인트 50 지급
        pointService.accruePointsAsync(1L, 50);

        // Then: 회원의 적립금이 100 + 50 = 150이어야 함
        Member member = memberRepository.findByMemberId(1L);
        assertEquals(150, member.getRewardPoints());
    }

    // 음수 포인트 적립 테스트
    @Test
    public void testAccrueMinusPoints() {
        // Given: 음수 포인트로 적립을 시도
        int negativePoints = -30;

        try {
            // When: 음수 포인트로 적립 시도
            pointService.accruePointsAsync(1L, negativePoints);
            fail("Fail: testAccrueMinusPoints");
        } catch (MemberServiceException e) {
            // Then: 예외 메시지를 확인하여 예상한 메시지와 일치하는지 검증
            assertEquals("적립 포인트는 1 이상이어야 합니다.", e.getMessage());

            // And: 회원의 적립금은 변화 없음 (음수로 증가하지 않아야 함)
            Member member = memberRepository.findByMemberId(1L);
            assertNotNull(member);
            assertEquals(100, member.getRewardPoints());
        }
    }

    // 적립금 사용 테스트
    @Test
    public void testUsePoints() {
        // When: 적립금 사용 30
        pointService.usePointsAsync(1L, 30);

        // Then: 회원의 적립금이 100 - 30 = 70이어야 함
        Member member = memberRepository.findByMemberId(1L);
        assertEquals(70, member.getRewardPoints());
    }

    // 적립금 취소 테스트
    @Test
    public void testCancelPoints() {
        // When: 적립금 사용
        pointService.usePointsAsync(1L, 30);

        // And: 사용한 포인트와 동일한 포인트를 취소
        int cancelPoints = 30;

        try {
            pointService.cancelPointsAsync(1L, cancelPoints);
            fail("Fail: testCancelPoints");
        } catch (MemberServiceException e) {
            // Then: 예외 메시지를 확인하여 예상한 메시지와 일치하는지 검증
            assertEquals("취소할 포인트가 부족합니다.", e.getMessage());

            // And: 회원의 적립금이 100 (변화 없음)
            Member member = memberRepository.findByMemberId(1L);
            assertEquals(100, member.getRewardPoints());
        }
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
