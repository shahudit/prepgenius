package com.prepgenius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepgenius.dto.LoginRequest;
import com.prepgenius.dto.QuestionRequest;
import com.prepgenius.model.*;
import com.prepgenius.repository.CategoryRepository;
import com.prepgenius.repository.CompanyRepository;
import com.prepgenius.repository.QuestionRepository;
import com.prepgenius.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminQuestionIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private Company tcs;
    private Category technical;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        companyRepository.deleteAll();
        categoryRepository.deleteAll();
        questionRepository.deleteAll();

        User admin = User.builder()
                .name("Admin")
                .email("admin@test.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);

        User user = User.builder()
                .name("User")
                .email("user@test.com")
                .passwordHash(passwordEncoder.encode("user123"))
                .role(UserRole.USER)
                .build();
        userRepository.save(user);

        adminToken = getToken("admin@test.com", "admin123");
        userToken = getToken("user@test.com", "user123");

        tcs = Company.builder().name("TCS").focus("IT").build();
        tcs = companyRepository.save(tcs);

        technical = Category.builder().name("Technical").group(InterviewMode.TECHNICAL).build();
        technical = categoryRepository.save(technical);
    }

    private String getToken(String email, String password) throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseString).get("token").asText();
    }

    @Test
    void testAdminCanCreateMCQ() throws Exception {
        QuestionRequest request = QuestionRequest.builder()
                .questionText("What is Java?")
                .type(QuestionType.MCQ)
                .difficulty(Difficulty.EASY)
                .categoryId(technical.getId())
                .companyIds(Collections.singletonList(tcs.getId()))
                .options(List.of("A", "B", "C", "D"))
                .correctIndex(0)
                .status(QuestionStatus.VERIFIED)
                .build();

        mockMvc.perform(post("/api/admin/questions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("What is Java?"))
                .andExpect(jsonPath("$.type").value("MCQ"));
    }

    @Test
    void testAdminCanCreateTextQuestion() throws Exception {
        QuestionRequest request = QuestionRequest.builder()
                .questionText("Explain JVM.")
                .type(QuestionType.TEXT)
                .difficulty(Difficulty.MEDIUM)
                .categoryId(technical.getId())
                .expectedAnswer("Java Virtual Machine...")
                .status(QuestionStatus.VERIFIED)
                .build();

        mockMvc.perform(post("/api/admin/questions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TEXT"));
    }

    @Test
    void testUserCannotCreateQuestion() throws Exception {
        QuestionRequest request = QuestionRequest.builder()
                .questionText("Hack?")
                .type(QuestionType.MCQ)
                .difficulty(Difficulty.EASY)
                .categoryId(technical.getId())
                .status(QuestionStatus.VERIFIED)
                .build();

        mockMvc.perform(post("/api/admin/questions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCorrectAnswerHiddenForUser() throws Exception {
        Question question = Question.builder()
                .questionText("Secret Question")
                .type(QuestionType.MCQ)
                .difficulty(Difficulty.EASY)
                .categoryId(technical.getId())
                .options(List.of("A", "B"))
                .correctIndex(1)
                .expectedAnswer("Secrets")
                .status(QuestionStatus.VERIFIED)
                .build();
        question = questionRepository.save(question);

        mockMvc.perform(get("/api/questions/" + question.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("Secret Question"))
                .andExpect(jsonPath("$.correctIndex").doesNotExist())
                .andExpect(jsonPath("$.expectedAnswer").doesNotExist());
    }

    @Test
    void testInvalidMCQRejected() throws Exception {
        QuestionRequest request = QuestionRequest.builder()
                .questionText("Bad MCQ")
                .type(QuestionType.MCQ)
                .difficulty(Difficulty.EASY)
                .categoryId(technical.getId())
                .options(List.of("Only One"))
                .correctIndex(0)
                .status(QuestionStatus.VERIFIED)
                .build();

        mockMvc.perform(post("/api/admin/questions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("MCQ must have at least 2 options"));
    }

    @Test
    void testFilteringWorks() throws Exception {
        Question question = Question.builder()
                .questionText("Filter Me")
                .type(QuestionType.MCQ)
                .difficulty(Difficulty.HARD)
                .categoryId(technical.getId())
                .companyIds(List.of(tcs.getId()))
                .status(QuestionStatus.VERIFIED)
                .build();
        questionRepository.save(question);

        mockMvc.perform(get("/api/questions")
                .param("companyId", tcs.getId())
                .param("difficulty", "HARD")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].questionText").value("Filter Me"));
    }
}
