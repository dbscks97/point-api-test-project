package com.marketboro.Premission;


import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.AccrueQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.service.AccruePointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AccruePointServiceTest {

    @InjectMocks
    private AccruePointService accruePointService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private AccrueQueueSender accrueQueueSender;

    private Member testMember;

    @BeforeEach
    public void setUp() {
        // Given: Mock 데이터 설정
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setMemberName("12345");
        testMember.setRewardPoints(100);

        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);
    }

    @Test
    public void 포인트적립실패_회원존재하지않음(){
        // given
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(null);

        // when
        final MemberException result = assertThrows(MemberException.class, () -> accruePointService.accruePointsAsync(testMember.getMemberId(), testMember.getMemberName(),50));

        // then
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.MEMBER_NOT_FOUND);
    }

    @Test
    public void 포인트적립실패_본인이아님(){
        // given
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);

        // when
        final MemberException result = assertThrows(MemberException.class, () -> accruePointService.accruePointsAsync(testMember.getMemberId(),"notowner",50));

        // then
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.NOT_MEMBER_OWNER);
    }
    @Test
    public void 양수포인트적립성공() {
        // given
        final int rewardPoints = 50;
        // when: 적립 포인트 50 지급
        CompletableFuture<Void> future = accruePointService.accruePointsAsync(testMember.getMemberId(),testMember.getMemberName(), rewardPoints);
        future.thenAccept(result -> {
            // then: 비동기 작업이 완료된 후에 결과를 검증하는 로직
            Member member = memberRepository.findByMemberId(testMember.getMemberId());
            assertThat(member.getRewardPoints()).isEqualTo(rewardPoints+ testMember.getRewardPoints());
        });
        future.join(); // 비동기 작업 완료를 기다림
    }


}
