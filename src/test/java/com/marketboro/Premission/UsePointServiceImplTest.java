package com.marketboro.Premission;


import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.messaging.senders.UseQueueSender;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.service.UsePointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsePointServiceImplTest {

    @InjectMocks
    private UsePointServiceImpl usePointService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private UseQueueSender useQueueSender;
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
    public void 포인트사용실패_회원존재하지않음(){
        // given
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(null);

        // when
        final MemberException result = assertThrows(MemberException.class, () -> usePointService.usePointsAsync(testMember.getMemberId(), testMember.getMemberName(),50));

        // then
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.MEMBER_NOT_FOUND);
    }

    @Test
    public void 포인트사용실패_본인이아님(){
        // given
        when(memberRepository.findByMemberId(testMember.getMemberId())).thenReturn(testMember);

        // when
        final MemberException result = assertThrows(MemberException.class, () -> usePointService.usePointsAsync(testMember.getMemberId(),"notowner",50));

        // then
        assertThat(result.getErrorResult()).isEqualTo(MemberErrorResult.NOT_MEMBER_OWNER);
    }
    @Test
    public void 포인트사용성공() {
        Member member = new Member();
        member.setMemberId(1L);
        member.setMemberName("12345");
        member.setRewardPoints(500);

        History history1 = new History();
        history1.setMember(member);
        history1.setPoints(100);
        history1.setCreatedAt(new Date());

        History history2 = new History();
        history2.setMember(member);
        history2.setPoints(200);
        history2.setCreatedAt(new Date());

        List<History> historyList = new ArrayList<>();
        historyList.add(history1);
        historyList.add(history2);

        when(memberRepository.findByMemberId(1L)).thenReturn(member);
        when(historyRepository.findByMember(member)).thenReturn(historyList);


        CompletableFuture<Void> result = usePointService.usePointsAsync(1L, testMember.getMemberName(),150);

        result.join();


        verify(historyRepository, times(1)).save(history1);
        verify(historyRepository, times(1)).save(history2);


        verify(useQueueSender, times(1)).sendUseMessage(1L, 150);
    }


}