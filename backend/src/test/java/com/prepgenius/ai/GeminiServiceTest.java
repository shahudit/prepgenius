package com.prepgenius.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepgenius.dto.AnswerEvaluationResponse;
import com.prepgenius.model.Category;
import com.prepgenius.model.Company;
import com.prepgenius.model.Difficulty;
import com.prepgenius.model.Question;
import com.prepgenius.model.QuestionStatus;
import com.prepgenius.model.QuestionType;
import com.prepgenius.repository.CategoryRepository;
import com.prepgenius.repository.CompanyRepository;
import com.prepgenius.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private QuestionRepository questionRepository;

    private GeminiService geminiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {

        geminiService = new GeminiService(
                geminiClient,
                companyRepository,
                categoryRepository,
                questionRepository,
                objectMapper
        );
    }

    @Test
    void testGenerateAndSaveQuestions() throws Exception {

        String companyId = "c1";
        String categoryId = "cat1";

        Company company = Company.builder()
                .id(companyId)
                .name("TCS")
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .name("Java")
                .description("Core Java")
                .build();

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        String mockResponse =
                "{"
                        + "\"questions\":["
                        + "{"
                        + "\"questionText\":\"Q1\","
                        + "\"questionType\":\"MCQ\","
                        + "\"difficulty\":\"EASY\","
                        + "\"options\":[\"A\",\"B\",\"C\",\"D\"],"
                        + "\"correctOptionIndex\":0,"
                        + "\"explanation\":\"E1\""
                        + "}"
                        + "]"
                        + "}";

        when(geminiClient.generateContent(anyString()))
                .thenReturn(mockResponse);

        when(questionRepository.save(any(Question.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Question> result =
                geminiService.generateAndSaveQuestions(
                        companyId,
                        categoryId,
                        Difficulty.EASY,
                        QuestionType.MCQ,
                        1
                );

        assertEquals(1, result.size());

        assertEquals(
                "Q1",
                result.get(0).getQuestionText()
        );

        assertEquals(
                QuestionStatus.AI_GENERATED,
                result.get(0).getStatus()
        );

        verify(
                questionRepository,
                times(1)
        ).save(any(Question.class));
    }

    @Test
    void testEvaluateAnswerSuccess() throws Exception {

        String question = "Q1";
        String expected = "A1";
        List<String> keywords = List.of("k1");
        String userAnswer = "UA1";

        String mockResponse =
                "{"
                        + "\"score\":90,"
                        + "\"feedback\":\"Good\","
                        + "\"matchedKeywords\":[\"k1\"]"
                        + "}";

        when(geminiClient.generateContent(anyString()))
                .thenReturn(mockResponse);

        AnswerEvaluationResponse response =
                geminiService.evaluateAnswer(
                        question,
                        expected,
                        keywords,
                        userAnswer
                );

        assertEquals(
                90,
                response.getScore()
        );

        assertEquals(
                "Good",
                response.getFeedback()
        );

        assertTrue(
                response.getMatchedKeywords()
                        .contains("k1")
        );
    }

    @Test
    void testEvaluateAnswerMalformedJsonThrowsException() {

        when(geminiClient.generateContent(anyString()))
                .thenReturn("invalid json");

        assertThrows(
                RuntimeException.class,
                () -> geminiService.evaluateAnswer(
                        "Q1",
                        "A1",
                        List.of("k1"),
                        "UA1"
                )
        );
    }
}