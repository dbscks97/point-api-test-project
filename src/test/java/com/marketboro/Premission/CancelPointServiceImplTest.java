package com.marketboro.Premission;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.CancelQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.service.CancelPointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CancelPointServiceImplTest {

    @InjectMocks
    private CancelPointServiceImpl cancelPointService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private CancelQueueSender cancelQueueSender;

    private Member testMember;

    @BeforeEach
    public void setUp() {
        // Given: Mock 데이터 설정
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setMemberName("12345");
        testMember.setRewardPoints(50);

        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);
    }

    @Test
    public void 포인트취소실패_회원존재하지않음(){
        // given
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(null);

        // when
        final MemberException result = assertThrows(MemberException.class, () -> cancelPointService.cancelPointsAsync(testMember.getMemberId(), testMember.getMemberName(),50,1));

        // then
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.MEMBER_NOT_FOUND);
    }

    @Test
    public void 포인트취소실패_본인이아님(){
        // given
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);

        // when
        final MemberException result = assertThrows(MemberException.class, () -> cancelPointService.cancelPointsAsync(testMember.getMemberId(),"notowner",50,1));

        // then
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.NOT_MEMBER_OWNER);
    }

    @Test
    public void 포인트취소성공() {
        // given
        List<History> historyList = new ArrayList<>();
        History history1 = new History();
        history1.setHistoryId(1L);
        history1.setMember(testMember);
        history1.setPoints(30); // 사용 이력
        history1.setType("use"); // 사용 이력 유형
        history1.setDeductPointNo(1); // 수정: deductPointNo 설정
        historyList.add(history1);

        History history2 = new History();
        history2.setHistoryId(2L);
        history2.setMember(testMember);
        history2.setPoints(20); // 사용 이력
        history2.setType("use"); // 사용 이력 유형
        history2.setDeductPointNo(2); // 수정: deductPointNo 설정
        historyList.add(history2);

        when(historyRepository.findByMemberAndTypeAndDeductPointNo(testMember, "use", 1)).thenReturn(historyList);
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);

        // when
        CompletableFuture<Void> future = cancelPointService.cancelPointsAsync(testMember.getMemberId(), testMember.getMemberName(), 30, 1);
        future.join(); // CompletableFuture를 기다림

        // 포인트가 제대로 취소되었는지 확인
        assertThat(testMember.getRewardPoints()).isEqualTo(50); // 보상 포인트는 그대로여야 함
        assertThat(history1.getPoints()).isEqualTo(0); // history1의 포인트는 0이어야 함
        assertThat(history2.getPoints()).isEqualTo(20); // history2의 포인트는 20이어야 함

        // 멤버 정보가 한 번 저장되었는지 확인
        verify(memberRepository, times(1)).save(testMember);

        // 메시지가 한 번 전송되었는지 확인
        verify(cancelQueueSender, times(1)).sendCancelMessage(testMember.getMemberId(), 30);
    }
}

