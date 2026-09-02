package com.prepgenius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepgenius.model.*;
import com.prepgenius.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InterviewHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private User otherUser;
    private Company company;
    private Category category;

    @BeforeEach
    void setUp() {
        interviewRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();
        categoryRepository.deleteAll();

        user = userRepository.save(
                User.builder()
                        .email("user@test.com")
                        .role(UserRole.USER)
                        .build()
        );

        otherUser = userRepository.save(
                User.builder()
                        .email("other@test.com")
                        .role(UserRole.USER)
                        .build()
        );

        company = companyRepository.save(
                Company.builder()
                        .name("TCS")
                        .build()
        );

        category = categoryRepository.save(
                Category.builder()
                        .name("Java")
                        .build()
        );
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void testGetHistorySuccess() throws Exception {

        Interview interview = Interview.builder()
                .userId(user.getId())
                .companyId(company.getId())
                .categoryId(category.getId())
                .interviewMode(InterviewMode.TECHNICAL)
                .difficulty(Difficulty.EASY)
                .questionType(QuestionType.MCQ)
                .numberOfQuestions(1)
                .questionIds(Collections.emptyList())
                .status(InterviewStatus.COMPLETED)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now())
                .answeredQuestions(1)
                .correctAnswers(1)
                .incorrectAnswers(0)
                .skippedQuestions(0)
                .totalScore(100.0)
                .percentage(100.0)
                .build();

        interviewRepository.save(interview);

        mockMvc.perform(
                        get("/api/interviews/history")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].companyName").value("TCS"))
                .andExpect(jsonPath("$.content[0].categoryName").value("Java"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void testCannotAccessOtherUserInterview() throws Exception {

        Interview otherInterview = Interview.builder()
                .userId(otherUser.getId())
                .companyId(company.getId())
                .categoryId(category.getId())
                .interviewMode(InterviewMode.TECHNICAL)
                .difficulty(Difficulty.EASY)
                .questionType(QuestionType.MCQ)
                .numberOfQuestions(1)
                .questionIds(Collections.emptyList())
                .status(InterviewStatus.COMPLETED)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now())
                .answeredQuestions(1)
                .correctAnswers(1)
                .incorrectAnswers(0)
                .skippedQuestions(0)
                .totalScore(100.0)
                .percentage(100.0)
                .build();

        otherInterview = interviewRepository.save(otherInterview);

        mockMvc.perform(
                        get("/api/interviews/" + otherInterview.getId())
                )
                .andExpect(status().isNotFound());
    }
}