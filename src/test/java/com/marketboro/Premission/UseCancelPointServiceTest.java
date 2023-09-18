//package com.marketboro.Premission;
//
//import com.marketboro.Premission.entity.Member;
//import com.marketboro.Premission.exception.MemberException;
//import org.junit.jupiter.api.Test;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class UseCancelPointServiceTest {
//
//    // 적립금 사용 테스트
//    @Test
//    public void testUsePoints() throws InterruptedException, ExecutionException {
//        // When: 적립금 사용 30
//        CompletableFuture<Void> future = accruePointService.usePointsAsync(1L, 30);
//        future.get(); // 비동기 작업 완료를 기다림
//
//        // Then: 회원의 적립금이 100 - 30 = 70이어야 함
//        Member member = memberRepository.findByMemberId(1L);
//        assertEquals(70, member.getRewardPoints());
//    }
//
//    // 적립금 취소 테스트
//    @Test
//    public void testCancelPoints() throws InterruptedException, ExecutionException {
//        // When: 적립금 사용
//        CompletableFuture<Void> useFuture = accruePointService.usePointsAsync(1L, 30);
//        useFuture.get(); // 적립금 사용 비동기 작업 완료를 기다림
//
//        // And: 사용한 포인트와 동일한 포인트를 취소
//        int cancelPoints = 30;
//
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            try {
//                accruePointService.cancelPointsAsync(1L, cancelPoints).get(); // 비동기 작업 완료를 기다림
//                fail("Fail: testCancelPoints");
//            } catch (ExecutionException | InterruptedException e) {
//                // Then: 예외 메시지를 확인하여 예상한 메시지와 일치하는지 검증
//                assertTrue(e.getCause() instanceof MemberException);
//                assertEquals("취소할 포인트가 부족합니다.", e.getCause().getMessage());
//            }
//        });
//
//        future.get(); // 비동기 작업 완료를 기다림
//
//        // And: 회원의 적립금이 100 (변화 없음)
//        Member member = memberRepository.findByMemberId(1L);
//        assertEquals(100, member.getRewardPoints());
//    }
//}
