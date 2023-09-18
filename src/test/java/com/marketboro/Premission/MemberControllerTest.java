package com.marketboro.Premission;


import com.google.gson.Gson;
import com.marketboro.Premission.controller.MemberController;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.request.MemberRequest;
import com.marketboro.Premission.response.MemberResponse;
import com.marketboro.Premission.service.AccruePointService;
import com.marketboro.Premission.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.marketboro.Premission.controller.MemberConstants.MEMBER_ID_HEADER;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberService memberService;

    @Mock
    private AccruePointService accruePointService;
    private MockMvc mockMvc;

    private Gson gson;

    private MemberRequest memberRequest(int rewardPoints) {
        return MemberRequest.builder()
                .rewardPoints(rewardPoints)
                .build();
    }
    @BeforeEach
    public void init(){
        gson = new Gson();
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .build();
    }
    @Test
    @DisplayName("[API][GET] 회원별 적립금 합계 조회 - 회원 식별값이 헤더에 없음")
    public void 적립금조회실패_회원식별값이헤더에없음() throws Exception {
        // given
        final String url = "/api/v1/123/points";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[API][GET] 회원별 적립금 합계 조회 - 회원이 존재하지 않음")
    public void 적립금조회실패_회원이존재하지않음() throws Exception {
        // given
        final String url = "/api/v1/-1/points";
        doThrow(new MemberException(MemberErrorResult.MEMBER_NOT_FOUND))
                .when(memberService)
                .getPoints(-1L, "12345");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .header(MEMBER_ID_HEADER,"12345")
        );

        // then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[API][GET] 회원별 적립금 합계 조회 - 성공")
    public void 적립금조회성공() throws Exception{
        // given
        final String url = "/api/v1/-1/points";
        doReturn(
                MemberResponse.builder().build()
        ).when(memberService).getPoints(-1L,"12345");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .header(MEMBER_ID_HEADER,"12345")

        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("[API][POST] 회원별 적립금 적립 - 회원 식별값이 헤더에 없음")
    public void 회원별적립실패_회원식별값이헤더에없음() throws Exception{
        // given
        final String url = "/api/v1/-1/accrue";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .header(MEMBER_ID_HEADER,"12345")
                        .content(gson.toJson(memberRequest(100)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[API][POST] 회원별 적립금 적립 - 포인트가 음수")
    public void 회원별적립실패_포인트가음수() throws Exception{
        // given
        final String url = "/api/v1/-1/accrue";

        //when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .header(MEMBER_ID_HEADER, "12345")
                        .content(gson.toJson(memberRequest(-1)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        resultActions.andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("[API][POST] 회원별 적립금 적립 - 성공")
    public void 회원별적립성공() throws Exception{
        // given
        final String url = "/api/v1/-1/accrue";

        //when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .header(MEMBER_ID_HEADER, "12345")
                        .param("points","100")
                        .content(gson.toJson(memberRequest(100)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        resultActions.andExpect(status().isNoContent());
    }




}
