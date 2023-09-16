package com.marketboro.Premission;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = PremissionApplication.class)
public class PointServiceTest {

    @Autowired
    private PointService pointService;

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
                .thenReturn(Collections.emptyList());
    }

    // 양수 포인트 적립 테스트
    @Test
    public void testAccruePositivePoints() throws InterruptedException, ExecutionException {
        // When: 적립 포인트 50 지급
        CompletableFuture<Void> future = pointService.accruePointsAsync(1L, 50);
        future.get(); // 비동기 작업 완료를 기다림

        // Then: 회원의 적립금이 100 + 50 = 150이어야 함
        Member member = memberRepository.findByMemberId(1L);
        assertEquals(150, member.getRewardPoints());
    }

    // 음수 포인트 적립 테스트
    @Test
    public void testAccrueMinusPoints() throws InterruptedException, ExecutionException {
        // Given: 음수 포인트로 적립을 시도
        int negativePoints = -30;

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                // When: 음수 포인트로 적립 시도
                pointService.accruePointsAsync(1L, negativePoints).get(); // 비동기 작업 완료를 기다림
                fail("Fail: testAccrueMinusPoints");
            } catch (ExecutionException | InterruptedException e) {
                // Then: 예외 메시지를 확인하여 예상한 메시지와 일치하는지 검증
                assertTrue(e.getCause() instanceof MemberException);
                assertEquals("적립 포인트는 1 이상이어야 합니다.", e.getCause().getMessage());
            }
        });

        future.get(); // 비동기 작업 완료를 기다림

        // And: 회원의 적립금은 변화 없음 (음수로 증가하지 않아야 함)
        Member member = memberRepository.findByMemberId(1L);
        assertNotNull(member);
        assertEquals(100, member.getRewardPoints());
    }

    // 적립금 사용 테스트
    @Test
    public void testUsePoints() throws InterruptedException, ExecutionException {
        // When: 적립금 사용 30
        CompletableFuture<Void> future = pointService.usePointsAsync(1L, 30);
        future.get(); // 비동기 작업 완료를 기다림

        // Then: 회원의 적립금이 100 - 30 = 70이어야 함
        Member member = memberRepository.findByMemberId(1L);
        assertEquals(70, member.getRewardPoints());
    }

    // 적립금 취소 테스트
    @Test
    public void testCancelPoints() throws InterruptedException, ExecutionException {
        // When: 적립금 사용
        CompletableFuture<Void> useFuture = pointService.usePointsAsync(1L, 30);
        useFuture.get(); // 적립금 사용 비동기 작업 완료를 기다림

        // And: 사용한 포인트와 동일한 포인트를 취소
        int cancelPoints = 30;

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                pointService.cancelPointsAsync(1L, cancelPoints).get(); // 비동기 작업 완료를 기다림
                fail("Fail: testCancelPoints");
            } catch (ExecutionException | InterruptedException e) {
                // Then: 예외 메시지를 확인하여 예상한 메시지와 일치하는지 검증
                assertTrue(e.getCause() instanceof MemberException);
                assertEquals("취소할 포인트가 부족합니다.", e.getCause().getMessage());
            }
        });

        future.get(); // 비동기 작업 완료를 기다림

        // And: 회원의 적립금이 100 (변화 없음)
        Member member = memberRepository.findByMemberId(1L);
        assertEquals(100, member.getRewardPoints());
    }
}
