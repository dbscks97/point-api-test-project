package com.marketboro.Premission.application;

import com.marketboro.Premission.dto.PointDto;
import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.CancelQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.service.CancelPointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("회원별 적립금 사용취소 API")
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
        // given
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
        history1.setPoints(30);
        history1.setType("use");
        history1.setDeductPointNo(1);
        historyList.add(history1);

        History history2 = new History();
        history2.setHistoryId(2L);
        history2.setMember(testMember);
        history2.setPoints(20);
        history2.setType("use");
        history2.setDeductPointNo(2);
        historyList.add(history2);

        when(historyRepository.findByMemberAndTypeAndDeductPointNo(testMember, "use", 1)).thenReturn(historyList);
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);

        // when
        CompletableFuture<PointDto.CancelPointResponse> future = cancelPointService.cancelPointsAsync(testMember.getMemberId(), testMember.getMemberName(), 30, 1);
        future.join();

        // then
        assertThat(testMember.getRewardPoints()).isEqualTo(50);
        assertThat(history1.getPoints()).isEqualTo(0);
        assertThat(history2.getPoints()).isEqualTo(20);

        verify(memberRepository, times(1)).save(testMember);

        verify(cancelQueueSender, times(1)).sendCancelMessage(testMember.getMemberId(), testMember.getMemberName(),30);
    }
}

