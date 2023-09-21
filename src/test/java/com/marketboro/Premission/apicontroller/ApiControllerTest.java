package com.marketboro.Premission.apicontroller;


import com.marketboro.Premission.controller.MemberController;
import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.restdocs.ControllerTestSupport;
import com.marketboro.Premission.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Date;

import static com.marketboro.Premission.controller.MemberConstants.MEMBER_ID_HEADER;
import static com.marketboro.Premission.restdocs.RestDocsCommonField.CODE;
import static com.marketboro.Premission.restdocs.RestDocsCommonField.MESSAGE;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class})
@DisplayName("API 컨트롤러 테스트")
public class ApiControllerTest extends ControllerTestSupport {
    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberServiceImpl memberService;
    @Mock
    private HistoryServiceImpl historyService;

    @Mock
    private AccruePointServiceImpl accruePointService;

    @Mock
    private UsePointServiceImpl usePointService;
    @Mock
    private CancelPointServiceImpl cancelPointService;


    @Test
    @DisplayName("[API][GET] 회원별 적립금 합계 조회 - 성공")
    public void 적립금조회성공() throws Exception {
        // given
        final Long memberId = 1L;

        // when
        final ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/v1/{memberId}/points", memberId)
                        .header(MEMBER_ID_HEADER, "12345")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(parameterWithName("memberId").description("회원번호")),
                        requestHeaders(headerWithName(MEMBER_ID_HEADER).description("회원 이름 헤더")),
                        responseFields(
                                fieldWithPath("data.points")
                                        .description("적립금 합계")
                                        .type(Integer.class)
                                        .optional(),
                                CODE,
                                MESSAGE
                        )

                ));
    }


    @Test
    @DisplayName("[API][POST] 회원별 적립금 적립 - 성공")
    public void 회원별적립성공() throws Exception {
        // given
        final Long memberId = 1L;

        //when
        final ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.post("/api/v1/{memberId}/accrue", memberId)
                        .header(MEMBER_ID_HEADER, "12345")
                        .param("point", "1000")
                        .content(gson.toJson(memberRequest(1000)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(parameterWithName("memberId").description("회원번호")),
                        requestHeaders(headerWithName(MEMBER_ID_HEADER).description("회원 이름 헤더")),
                        requestFields(
                                fieldWithPath("points").description("적립 포인트").type(Integer.class).optional()
                        ),
                        responseFields(
                                fieldWithPath("data.points")
                                        .description("적립 포인트")
                                        .type(Integer.class)
                                        .optional(),
                                CODE,
                                MESSAGE
                        )

                ));
    }


    @Test
    @DisplayName("[API][GET] 회원별 적립금/사용 내역 조회[페이징] - 성공")
    public void 적립금사용내역조회성공() throws Exception {
        // given
        final Long memberId = 1L;

        Member member = memberRepository.findByMemberId(memberId);

        // History 데이터 생성
        History history = new History();
        history.setPoints(1000);
        history.setCreatedAt(new Date());
        history.setUpdatedAt(new Date());
        history.setType("적립");
        history.setMember(member);
        historyRepository.save(history);

        // when
        final ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/v1/{memberId}/point-history", memberId)
                        .header(MEMBER_ID_HEADER, "12345")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(parameterWithName("memberId").description("회원번호")),
                        requestHeaders(headerWithName(MEMBER_ID_HEADER).description("회원 이름 헤더")),
                        responseFields(
                                fieldWithPath("data.history[].historyId").description("적립금/사용 내역 식별자"),
                                fieldWithPath("data.history[].points").description("적립/사용 금액"),
                                fieldWithPath("data.history[].type").description("내역 구분"),
                                fieldWithPath("data.history[].createdAt").description("등록 일자"),
                                fieldWithPath("data.history[].updatedAt").description("수정 일자"),
                                CODE,
                                MESSAGE
                        )
                ));

    }

    @Test
    @DisplayName("[API][POST] 회원별 적립금 사용 - 성공")
    public void 회원별적립사용성공() throws Exception {
        // given
        final Long memberId = 1L;

        // when
        final ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.post("/api/v1/{memberId}/use",memberId)
                        .header(MEMBER_ID_HEADER, "12345")
                        .param("pointsToUse","100")
                        .content(gson.toJson(memberRequest(100)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(parameterWithName("memberId").description("회원번호")),
                        requestHeaders(headerWithName(MEMBER_ID_HEADER).description("회원 이름 헤더")),
                        requestFields(
                                fieldWithPath("points").description("사용 포인트").type(Integer.class).optional()
                        ),
                        responseFields(
                                fieldWithPath("data.pointsUsed").description("사용 포인트").type(Integer.class).optional(),
                                fieldWithPath("data.deductPointNo").description("포인트 차감번호").type(Integer.class).optional(),
                                CODE,
                                MESSAGE
                        )

                ));

    }


    @Test
    @DisplayName("[API][POST] 회원별 적립금 사용 취소 - 성공")
    public void 회원별적립금취소성공() throws Exception {
        final Long memberId = 1L;
        int deductPointNo = 1;

        Member member = new Member();
        member.setRewardPoints(50);

        // when
        final ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.post("/api/v1/{memberId}/cancel",memberId)
                        .header(MEMBER_ID_HEADER, "12345")
                        .param("pointsToCancel","100")
                        .param("deductPointNo", String.valueOf(deductPointNo))
                        .content(gson.toJson(memberRequest(100)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(parameterWithName("memberId").description("회원번호")),
                        requestHeaders(headerWithName(MEMBER_ID_HEADER).description("회원 이름 헤더")),
                        requestFields(
                                fieldWithPath("points").description("사용 포인트").type(Integer.class).optional(),
                                fieldWithPath("deductPointNo").description("포인트 차감번호").type(Integer.class).optional()
                        ),
                        responseFields(
                                fieldWithPath("data.pointCanceled").description("사용 포인트").type(Integer.class).optional(),
                                fieldWithPath("data.deductPointNo").description("포인트 차감번호").type(Integer.class).optional(),
                                CODE,
                                MESSAGE
                        )
                ));

    }

}
