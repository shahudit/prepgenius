package com.prepgenius.service;

import com.prepgenius.ai.GeminiService;
import com.prepgenius.dto.QuestionForInterviewResponse;
import com.prepgenius.dto.QuestionRequest;
import com.prepgenius.dto.QuestionResponse;
import com.prepgenius.model.Category;
import com.prepgenius.model.Company;
import com.prepgenius.model.Difficulty;
import com.prepgenius.model.InterviewMode;
import com.prepgenius.model.Question;
import com.prepgenius.model.QuestionStatus;
import com.prepgenius.model.QuestionType;
import com.prepgenius.repository.CategoryRepository;
import com.prepgenius.repository.CompanyRepository;
import com.prepgenius.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final GeminiService geminiService;

    public List<QuestionResponse> getOrGenerateQuestions(
            String companyId,
            String categoryId,
            Difficulty difficulty,
            QuestionType type,
            int requiredCount
    ) {
        return getOrGenerateQuestions(
                companyId,
                categoryId,
                InterviewMode.TECHNICAL,
                difficulty,
                type,
                requiredCount
        );
    }

    public List<QuestionResponse> getOrGenerateQuestions(
            String companyId,
            String categoryId,
            InterviewMode interviewMode,
            Difficulty difficulty,
            QuestionType type,
            int requiredCount
    ) {

        if (companyId == null || companyId.isBlank()) {
            throw new RuntimeException("Company ID is required");
        }

        if (interviewMode == null) {
            throw new RuntimeException("Interview type is required");
        }

        if (difficulty == null) {
            throw new RuntimeException("Difficulty is required");
        }

        if (type == null) {
            throw new RuntimeException("Question type is required");
        }

        if (requiredCount <= 0) {
            throw new RuntimeException(
                    "Number of questions must be greater than zero"
            );
        }

        if (interviewMode == InterviewMode.TECHNICAL) {

            if (categoryId == null || categoryId.isBlank()) {
                throw new RuntimeException(
                        "Technical domain is required for technical interviews"
                );
            }

            categoryRepository.findById(categoryId)
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "Technical domain not found"
                            )
                    );
        }

        List<Question> allQuestions =
                questionRepository.findAll();

        List<Question> matchingQuestions =
                allQuestions.stream()
                        .filter(Objects::nonNull)
                        .filter(question ->
                                question.getCompanyIds() != null &&
                                        question.getCompanyIds()
                                                .contains(companyId)
                        )
                        .filter(question ->
                                question.getDifficulty() == difficulty
                        )
                        .filter(question ->
                                question.getType() == type
                        )
                        .filter(question ->

                                question.getInterviewMode() ==
                                        interviewMode
                        )
                        .filter(question -> {

                            if (interviewMode ==
                                    InterviewMode.TECHNICAL) {

                                return Objects.equals(
                                        question.getCategoryId(),
                                        categoryId
                                );
                            }

                            return true;
                        })
                        .collect(Collectors.toList());

        if (matchingQuestions.size() >= requiredCount) {

            return matchingQuestions.stream()
                    .limit(requiredCount)
                    .map(this::mapToResponse)
                    .toList();
        }

        int missingCount =
                requiredCount - matchingQuestions.size();

        List<Question> aiQuestions =
                geminiService.generateAndSaveQuestions(
                        companyId,
                        categoryId,
                        interviewMode,
                        difficulty,
                        type,
                        missingCount
                );

        if (aiQuestions != null) {
            matchingQuestions.addAll(aiQuestions);
        }

        return matchingQuestions.stream()
                .limit(requiredCount)
                .map(this::mapToResponse)
                .toList();
    }

    public QuestionResponse createQuestion(
            QuestionRequest request,
            String adminEmail
    ) {

        validateQuestionRequest(request);

        InterviewMode resolvedMode =
                request.getInterviewMode() != null
                        ? request.getInterviewMode()
                        : InterviewMode.TECHNICAL;

        Question question =
                Question.builder()
                        .questionText(
                                request.getQuestionText()
                        )
                        .type(
                                request.getType()
                        )
                        .difficulty(
                                request.getDifficulty()
                        )
                        .categoryId(
                                resolvedMode == InterviewMode.TECHNICAL
                                        ? request.getCategoryId()
                                        : null
                        )
                        .interviewMode(
                                resolvedMode
                        )
                        .companyIds(
                                request.getCompanyIds()
                        )
                        .options(
                                request.getOptions()
                        )
                        .correctIndex(
                                request.getCorrectIndex()
                        )
                        .expectedAnswer(
                                request.getExpectedAnswer()
                        )
                        .idealKeywords(
                                request.getIdealKeywords()
                        )
                        .explanation(
                                request.getExplanation()
                        )
                        .evaluationCriteria(
                                request.getEvaluationCriteria()
                        )
                        .maxScore(
                                request.getMaxScore()
                        )
                        .status(
                                request.getStatus()
                        )
                        .sourceType(
                                request.getSourceType()
                        )
                        .sourceReference(
                                request.getSourceReference()
                        )
                        .sourceUrl(
                                request.getSourceUrl()
                        )
                        .createdBy(
                                adminEmail
                        )
                        .build();

        question =
                questionRepository.save(question);

        return mapToResponse(question);
    }

    public Page<QuestionResponse> getQuestions(
            String companyId,
            String categoryId,
            InterviewMode interviewMode,
            Difficulty difficulty,
            QuestionType type,
            QuestionStatus status,
            Pageable pageable
    ) {

        List<Question> filtered =
                questionRepository.findAll()
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(question -> {

                            if (companyId == null ||
                                    companyId.isBlank()) {
                                return true;
                            }

                            return question.getCompanyIds() != null &&
                                    question.getCompanyIds()
                                            .contains(companyId);
                        })
                        .filter(question -> {

                            if (categoryId == null ||
                                    categoryId.isBlank()) {
                                return true;
                            }

                            return Objects.equals(
                                    question.getCategoryId(),
                                    categoryId
                            );
                        })
                        .filter(question -> {

                            if (interviewMode == null) {
                                return true;
                            }

                            return question.getInterviewMode()
                                    == interviewMode;
                        })
                        .filter(question -> {

                            if (difficulty == null) {
                                return true;
                            }

                            return question.getDifficulty()
                                    == difficulty;
                        })
                        .filter(question -> {

                            if (type == null) {
                                return true;
                            }

                            return question.getType()
                                    == type;
                        })
                        .filter(question -> {

                            if (status == null) {
                                return true;
                            }

                            return question.getStatus()
                                    == status;
                        })
                        .collect(Collectors.toList());

        int pageSize =
                pageable.isPaged()
                        ? pageable.getPageSize()
                        : filtered.size();

        int start =
                pageable.isPaged()
                        ? (int) pageable.getOffset()
                        : 0;

        if (start >= filtered.size()) {

            return new PageImpl<>(
                    List.of(),
                    pageable,
                    filtered.size()
            );
        }

        int end =
                Math.min(
                        start + pageSize,
                        filtered.size()
                );

        List<QuestionResponse> content =
                filtered.subList(start, end)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return new PageImpl<>(
                content,
                pageable,
                filtered.size()
        );
    }

    public QuestionResponse getQuestionById(
            String id
    ) {

        if (id == null || id.isBlank()) {
            throw new RuntimeException(
                    "Question ID is required"
            );
        }

        Question question =
                questionRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Question not found"
                                )
                        );

        return mapToResponse(question);
    }

    public QuestionForInterviewResponse getQuestionForInterview(
            String id
    ) {

        if (id == null || id.isBlank()) {
            throw new RuntimeException(
                    "Question ID is required"
            );
        }

        Question question =
                questionRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Question not found"
                                )
                        );

        return mapToInterviewResponse(question);
    }

    public QuestionResponse updateQuestion(
            String id,
            QuestionRequest request
    ) {

        Question question =
                questionRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Question not found"
                                )
                        );

        validateQuestionRequest(request);

        InterviewMode resolvedMode =
                request.getInterviewMode() != null
                        ? request.getInterviewMode()
                        : InterviewMode.TECHNICAL;

        question.setQuestionText(
                request.getQuestionText()
        );

        question.setType(
                request.getType()
        );

        question.setDifficulty(
                request.getDifficulty()
        );

        question.setCategoryId(
                resolvedMode == InterviewMode.TECHNICAL
                        ? request.getCategoryId()
                        : null
        );

        question.setInterviewMode(
                resolvedMode
        );

        question.setCompanyIds(
                request.getCompanyIds()
        );

        question.setOptions(
                request.getOptions()
        );

        question.setCorrectIndex(
                request.getCorrectIndex()
        );

        question.setExpectedAnswer(
                request.getExpectedAnswer()
        );

        question.setIdealKeywords(
                request.getIdealKeywords()
        );

        question.setExplanation(
                request.getExplanation()
        );

        question.setEvaluationCriteria(
                request.getEvaluationCriteria()
        );

        question.setMaxScore(
                request.getMaxScore()
        );

        question.setStatus(
                request.getStatus()
        );

        question.setSourceType(
                request.getSourceType()
        );

        question.setSourceReference(
                request.getSourceReference()
        );

        question.setSourceUrl(
                request.getSourceUrl()
        );

        question =
                questionRepository.save(question);

        return mapToResponse(question);
    }

    public void deleteQuestion(
            String id
    ) {

        if (!questionRepository.existsById(id)) {
            throw new RuntimeException(
                    "Question not found"
            );
        }

        questionRepository.deleteById(id);
    }

    private void validateQuestionRequest(
            QuestionRequest request
    ) {

        InterviewMode resolvedMode =
                request.getInterviewMode() != null
                        ? request.getInterviewMode()
                        : InterviewMode.TECHNICAL;

        if (resolvedMode == InterviewMode.TECHNICAL) {

            if (request.getCategoryId() == null ||
                    !categoryRepository.existsById(
                            request.getCategoryId()
                    )) {

                throw new RuntimeException(
                        "Category not found"
                );
            }

        } else if (request.getCategoryId() != null &&
                !request.getCategoryId().isBlank()) {

            throw new RuntimeException(
                    "Category must not be set for HR, Aptitude or Mixed questions"
            );
        }

        if (request.getCompanyIds() != null) {

            for (String companyId :
                    request.getCompanyIds()) {

                if (!companyRepository.existsById(
                        companyId
                )) {

                    throw new RuntimeException(
                            "Company not found: "
                                    + companyId
                    );
                }
            }
        }

        if (request.getType() == QuestionType.MCQ) {

            if (request.getOptions() == null ||
                    request.getOptions().size() < 2) {

                throw new RuntimeException(
                        "MCQ must have at least 2 options"
                );
            }

            if (request.getCorrectIndex() == null ||
                    request.getCorrectIndex() < 0 ||
                    request.getCorrectIndex() >=
                            request.getOptions().size()) {

                throw new RuntimeException(
                        "Invalid correct option index"
                );
            }
        }
    }

    private QuestionResponse mapToResponse(
            Question question
    ) {

        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(
                        question.getQuestionText()
                )
                .type(
                        question.getType()
                )
                .difficulty(
                        question.getDifficulty()
                )
                .categoryId(
                        question.getCategoryId()
                )
                .interviewMode(
                        question.getInterviewMode()
                )
                .companyIds(
                        question.getCompanyIds()
                )
                .options(
                        question.getOptions()
                )
                .correctIndex(
                        question.getCorrectIndex()
                )
                .expectedAnswer(
                        question.getExpectedAnswer()
                )
                .idealKeywords(
                        question.getIdealKeywords()
                )
                .explanation(
                        question.getExplanation()
                )
                .evaluationCriteria(
                        question.getEvaluationCriteria()
                )
                .maxScore(
                        question.getMaxScore()
                )
                .status(
                        question.getStatus()
                )
                .sourceType(
                        question.getSourceType()
                )
                .sourceReference(
                        question.getSourceReference()
                )
                .sourceUrl(
                        question.getSourceUrl()
                )
                .createdBy(
                        question.getCreatedBy()
                )
                .createdAt(
                        question.getCreatedAt()
                )
                .updatedAt(
                        question.getUpdatedAt()
                )
                .build();
    }

    private QuestionForInterviewResponse mapToInterviewResponse(
            Question question
    ) {

        return QuestionForInterviewResponse.builder()
                .id(question.getId())
                .questionText(
                        question.getQuestionText()
                )
                .type(
                        question.getType()
                )
                .difficulty(
                        question.getDifficulty()
                )
                .categoryId(
                        question.getCategoryId()
                )
                .companyIds(
                        question.getCompanyIds()
                )
                .options(
                        question.getOptions()
                )
                .build();
    }
}