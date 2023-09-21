package com.marketboro.Premission.restdocs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.marketboro.Premission.controller.MemberController;
import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.repository.HistoryRepository;
import com.marketboro.Premission.repository.MemberRepository;
import com.marketboro.Premission.request.MemberRequest;
import com.marketboro.Premission.service.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import(RestDocsConfiguration.class)
public class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired protected RestDocumentationResultHandler restDocs;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected EntityManager entityManager;
    @Autowired
    protected MemberRepository memberRepository;
    @Autowired
    protected HistoryRepository historyRepository;
    protected Pageable pageable;


    protected Gson gson;

    @Transactional
    @BeforeEach
    void setUp(
            final WebApplicationContext context,
            final RestDocumentationContextProvider provider
    ) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(provider).uris().withPort(8500))
                .alwaysDo(MockMvcResultHandlers.print())
                .alwaysDo(restDocs)
                .build();
        gson = new Gson();
        objectMapper = new ObjectMapper();

        Member mockMember = new Member();
        mockMember.setMemberId(1L);
        mockMember.setMemberName("12345");
        mockMember.setRewardPoints(10000);
        memberRepository.save(mockMember);

        Calendar calendar = Calendar.getInstance();
        Date accrueDate = calendar.getTime();

        History history = new History();
        history.setHistoryId(1L);
        history.setPoints(1000);
        history.setCreatedAt(LocalDateTime.now());
        history.setUpdatedAt(LocalDateTime.now());
        history.setHistoryDate(accrueDate);
        history.setType("적립");
        history.setMember(mockMember);
        historyRepository.save(history);
    }

    protected MemberRequest memberRequest(int points) {
        return MemberRequest.builder()
                .points(points)
                .build();
    }
    protected String extractNextPageToken(ResultActions resultActions) throws Exception {
        String content = resultActions.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);
        return jsonNode.path("nextPageToken").asText();
    }
}
