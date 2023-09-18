package com.marketboro.Premission;


import com.google.gson.Gson;
import com.marketboro.Premission.controller.MemberController;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.response.MemberResponse;
import com.marketboro.Premission.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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


    private MockMvc mockMvc;

    private Gson gson;
    @BeforeEach
    public void init(){
        gson = new Gson();
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .build();
    }
    @Test
    public void 적립금조회실패_회원식별값이헤더에없음() throws Exception {
        // given
        final String url = "/api/v1/points/123";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void 적립금조회실패_회원이존재하지않음() throws Exception {
        // given
        final String url = "/api/v1/points/-1";
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
    public void 적립금조회성공() throws Exception{
        // given
        final String url = "/api/v1/points/-1";
        doReturn(
                MemberResponse.builder().build()
        ).when(memberService).getPoints(-1L,"12345");

        //when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .header(MEMBER_ID_HEADER,"12345")

        );

        //then
        resultActions.andExpect(status().isOk());
    }


}
