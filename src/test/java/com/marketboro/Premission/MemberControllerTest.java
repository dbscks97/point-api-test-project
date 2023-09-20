package com.marketboro.Premission;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.marketboro.Premission.controller.MemberController;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.request.MemberRequest;
import com.marketboro.Premission.response.MemberResponse;
import com.marketboro.Premission.service.AccruePointServiceImpl;
import com.marketboro.Premission.service.HistoryServiceImpl;
import com.marketboro.Premission.service.MemberServiceImpl;
import com.marketboro.Premission.service.UsePointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.marketboro.Premission.controller.MemberConstants.MEMBER_ID_HEADER;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@ExtendWith(MockitoExtension.class)
public class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberServiceImpl memberServiceImpl;
    @Mock
    private HistoryServiceImpl historyServiceImpl;

    @Mock
    private AccruePointServiceImpl accruePointService;

    @Mock
    private UsePointServiceImpl usePointService;
    @Mock
    private MemberRepository memberRepository;
    private Pageable pageable;

    private MockMvc mockMvc;

    private Gson gson;
    private ObjectMapper objectMapper;

    private MemberRequest memberRequest(int points) {
        return MemberRequest.builder()
                .points(points)
                .build();
    }
    // 이 메서드는 결과에서 다음 페이지 토큰을 추출합니다.
    private String extractNextPageToken(ResultActions resultActions) throws Exception {
        String content = resultActions.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);
        return jsonNode.path("nextPageToken").asText();
    }
    @BeforeEach
    public void init(){
        gson = new Gson();
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .build();
        pageable = PageRequest.of(0,10);
        objectMapper = new ObjectMapper();
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
        final String url = "/api/v1/1/points";
        doThrow(new MemberException(MemberErrorResult.MEMBER_NOT_FOUND))
                .when(memberServiceImpl)
                .getPoints(1L, "12345");

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
        final String url = "/api/v1/1/points";
        doReturn(
                MemberResponse.builder().build()
        ).when(memberServiceImpl).getPoints(1L,"12345");

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
        final String url = "/api/v1/1/accrue";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[API][POST] 회원별 적립금 적립 - 포인트가 음수")
    public void 회원별적립실패_포인트가음수() throws Exception{
        // given
        final String url = "/api/v1/1/accrue";

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


    @Test
    @DisplayName("[API][GET] 회원별 적립금/사용 내역 조회[페이징] - 회원 식별값이 헤더에 없음")
    public void 내역조회실패_회원식별값이헤더에없음() throws Exception{
        // given
        final String url = "/api/v1/-1/point-history";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("[API][GET] 회원별 적립금/사용 내역 조회[페이징] - 성공")
    public void 적립금사용내역조회성공() throws Exception {
        // given
        final String url = "/api/v1/-1/point-history";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .header(MEMBER_ID_HEADER, "12345")
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize()))
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("[API][GET] 회원별 적립금/사용 내역 조회[페이징] - 다음 페이지 조회")
    public void 적립금사용내역_다음페이지조회() throws Exception {
        // given
        final String url = "/api/v1/-1/point-history";
        final int pageSize = 10;

        // when
        // 첫 번째 페이지를 조회합니다.
        final ResultActions firstPageResult = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .header(MEMBER_ID_HEADER, "12345")
                        .param("page", "0") // 첫 번째 페이지
                        .param("size", String.valueOf(pageSize))
        );

        // then
        firstPageResult.andExpect(status().isOk());

        // 첫 번째 페이지 결과에서 다음 페이지 토큰을 추출합니다.
        String nextPageToken = extractNextPageToken(firstPageResult);

        // 다음 페이지를 조회합니다.
        final ResultActions nextPageResult = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .header(MEMBER_ID_HEADER, "12345")
                        .param("page", nextPageToken) // 다음 페이지 토큰 사용
                        .param("size", String.valueOf(pageSize))
        );

        // then
        nextPageResult.andExpect(status().isOk());
    }

    @Test
    @DisplayName("[API][POST] 회원별 적립금 사용 - 성공")
    public void 회원별적립사용성공() throws Exception {
        // given
        final String url = "/api/v1/1/use";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .header(MEMBER_ID_HEADER, "12345")
                        .param("pointsToUse","100")
                        .content(gson.toJson(memberRequest(100)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isNoContent());

    }


    @Test
    @DisplayName("[API][POST] 회원별 적립금 사용 - 포인트가 음수")
    public void 회원별적립사용실패_포인트가음수() throws Exception {
        // given
        final String url = "/api/v1/1/use";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .header(MEMBER_ID_HEADER, "12345")
                        .content(gson.toJson(memberRequest(-1)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[API][POST] 회원별 적립금 사용 - 보유 포인트 부족")
    public void 회원별적립사용실패_보유포인트부족() throws Exception {
        // given
        final String url = "/api/v1/1/use";

        Member member = new Member();
        member.setRewardPoints(200); // 사용자의 보유 포인트 설정

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .header(MEMBER_ID_HEADER, "12345")
                        .content(gson.toJson(memberRequest(300)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

}
