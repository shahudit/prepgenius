package com.prepgenius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepgenius.dto.StartInterviewRequest;
import com.prepgenius.model.Category;
import com.prepgenius.model.Company;
import com.prepgenius.model.Difficulty;
import com.prepgenius.model.Question;
import com.prepgenius.model.QuestionType;
import com.prepgenius.model.User;
import com.prepgenius.model.UserRole;
import com.prepgenius.repository.CategoryRepository;
import com.prepgenius.repository.CompanyRepository;
import com.prepgenius.repository.QuestionRepository;
import com.prepgenius.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InterviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Company company;
    private Category category;

    @BeforeEach
    void setUp() {

        questionRepository.deleteAll();
        categoryRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(
                User.builder()
                        .email("user@test.com")
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
                        .description("Core Java")
                        .build()
        );
    }

    @Test
    @WithMockUser(
            username = "user@test.com",
            roles = {"USER"}
    )
    void testStartInterviewSuccess() throws Exception {

        Question question = Question.builder()
                .questionText("What is JVM?")
                .categoryId(category.getId())
                .companyIds(List.of(company.getId()))
                .type(QuestionType.MCQ)
                .difficulty(Difficulty.EASY)
                .options(List.of(
                        "Java Virtual Machine",
                        "Java Variable Method",
                        "Java Version Manager",
                        "None of these"
                ))
                .correctIndex(0)
                .maxScore(100)
                .build();

        questionRepository.save(question);

        StartInterviewRequest request = StartInterviewRequest.builder()
                .companyId(company.getId())
                .categoryId(category.getId())
                .difficulty(Difficulty.EASY)
                .questionType(QuestionType.MCQ)
                .numberOfQuestions(1)
                .build();

        mockMvc.perform(
                        post("/api/interviews/start")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());
    }
}