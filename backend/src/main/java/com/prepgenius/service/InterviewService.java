package com.prepgenius.service;

import com.prepgenius.ai.GeminiService;
import com.prepgenius.dto.AnswerEvaluationResponse;
import com.prepgenius.dto.InterviewDetailsResponse;
import com.prepgenius.dto.InterviewHistoryResponse;
import com.prepgenius.dto.ProgressResponse;
import com.prepgenius.dto.QuestionForInterviewResponse;
import com.prepgenius.dto.QuestionResponse;
import com.prepgenius.dto.StartInterviewRequest;
import com.prepgenius.dto.StartInterviewResponse;
import com.prepgenius.dto.SubmitAnswerRequest;
import com.prepgenius.dto.SubmitAnswerResponse;
import com.prepgenius.model.Category;
import com.prepgenius.model.Company;
import com.prepgenius.model.Difficulty;
import com.prepgenius.model.Interview;
import com.prepgenius.model.InterviewAnswer;
import com.prepgenius.model.InterviewMode;
import com.prepgenius.model.InterviewStatus;
import com.prepgenius.model.Question;
import com.prepgenius.model.QuestionType;
import com.prepgenius.repository.CategoryRepository;
import com.prepgenius.repository.CompanyRepository;
import com.prepgenius.repository.InterviewAnswerRepository;
import com.prepgenius.repository.InterviewRepository;
import com.prepgenius.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final QuestionService questionService;
    private final GeminiService geminiService;

    public StartInterviewResponse startInterview(
            StartInterviewRequest request,
            String userId
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Interview request is required"
            );
        }

        if (userId == null ||
                userId.isBlank()) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        if (request.getCompanyId() == null ||
                request.getCompanyId().isBlank()) {

            throw new RuntimeException(
                    "Company is required"
            );
        }

        Company company =
                companyRepository.findById(
                                request.getCompanyId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Company not found"
                                )
                        );

        if (request.getInterviewMode() == null) {

            throw new RuntimeException(
                    "Interview type is required"
            );
        }

        InterviewMode interviewMode =
                request.getInterviewMode();

        String categoryId =
                request.getCategoryId();

        if (interviewMode ==
                InterviewMode.TECHNICAL) {

            if (categoryId == null ||
                    categoryId.isBlank()) {

                throw new RuntimeException(
                        "Technical domain is required for technical interviews"
                );
            }

            categoryRepository.findById(
                            categoryId
                    )
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "Technical domain not found"
                            )
                    );

        } else {

            categoryId = null;
        }

        QuestionType questionType =
                request.getQuestionType();

        if (questionType == null) {

            throw new RuntimeException(
                    "Question type is required"
            );
        }

        if (interviewMode ==
                InterviewMode.HR &&
                questionType != QuestionType.TEXT) {

            throw new RuntimeException(
                    "HR interviews use descriptive text answers only"
            );
        }

        if (interviewMode ==
                InterviewMode.TECHNICAL &&
                questionType != QuestionType.MCQ) {

            throw new RuntimeException(
                    "Technical interviews use MCQ questions only"
            );
        }

        if (interviewMode ==
                InterviewMode.APTITUDE &&
                questionType != QuestionType.MCQ) {

            throw new RuntimeException(
                    "Aptitude interviews use MCQ questions only"
            );
        }

        List<QuestionResponse> questions =
                questionService.getOrGenerateQuestions(
                        request.getCompanyId(),
                        categoryId,
                        interviewMode,
                        request.getDifficulty(),
                        questionType,
                        request.getNumberOfQuestions()
                );

        if (questions == null ||
                questions.isEmpty()) {

            throw new RuntimeException(
                    "No questions could be generated for the selected interview type"
            );
        }

        int actualQuestionCount =
                questions.size();

        Interview session =
                Interview.builder()
                        .userId(userId)

                        .companyId(
                                company.getId()
                        )

                        .categoryId(
                                categoryId
                        )

                        .interviewMode(
                                interviewMode
                        )

                        .difficulty(
                                request.getDifficulty()
                        )

                        .questionType(
                                questionType
                        )

                        .numberOfQuestions(
                                actualQuestionCount
                        )

                        .questionIds(
                                questions.stream()
                                        .map(
                                                QuestionResponse::getId
                                        )
                                        .collect(
                                                Collectors.toList()
                                        )
                        )

                        .answeredQuestions(0)
                        .correctAnswers(0)
                        .incorrectAnswers(0)
                        .skippedQuestions(0)

                        .totalScore(0.0)
                        .percentage(0.0)

                        .status(
                                InterviewStatus.IN_PROGRESS
                        )

                        .startTime(
                                LocalDateTime.now()
                        )

                        .build();

        session =
                interviewRepository.save(
                        session
                );

        return StartInterviewResponse
                .builder()
                .interviewId(
                        session.getId()
                )
                .companyId(
                        session.getCompanyId()
                )
                .categoryId(
                        session.getCategoryId()
                )
                .difficulty(
                        session.getDifficulty()
                )
                .questionType(
                        session.getQuestionType()
                )
                .totalQuestions(
                        session.getNumberOfQuestions()
                )
                .questions(
                        questions.stream()
                                .map(
                                        q ->
                                                QuestionForInterviewResponse
                                                        .builder()
                                                        .id(
                                                                q.getId()
                                                        )
                                                        .questionText(
                                                                q.getQuestionText()
                                                        )
                                                        .type(
                                                                q.getType()
                                                        )
                                                        .difficulty(
                                                                q.getDifficulty()
                                                        )
                                                        .categoryId(
                                                                q.getCategoryId()
                                                        )
                                                        .companyIds(
                                                                q.getCompanyIds()
                                                        )
                                                        .options(
                                                                q.getOptions()
                                                        )
                                                        .build()
                                )
                                .collect(
                                        Collectors.toList()
                                )
                )
                .startedAt(
                        session.getStartTime()
                )
                .status(
                        session.getStatus()
                )
                .build();
    }

    public SubmitAnswerResponse submitAnswer(
            String interviewId,
            SubmitAnswerRequest request,
            String userId
    ) {

        Interview session =
                interviewRepository
                        .findByIdAndUserId(
                                interviewId,
                                userId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Interview not found"
                                )
                        );

        if (session.getStatus() !=
                InterviewStatus.IN_PROGRESS) {

            throw new RuntimeException(
                    "Interview is not in progress"
            );
        }

        if (request == null ||
                request.getQuestionId() == null) {

            throw new RuntimeException(
                    "Question ID is required"
            );
        }

        if (session.getQuestionIds() == null ||
                !session.getQuestionIds()
                        .contains(
                                request.getQuestionId()
                        )) {

            throw new RuntimeException(
                    "Question does not belong to this interview"
            );
        }

        if (answerRepository
                .findByInterviewSessionIdAndQuestionId(
                        interviewId,
                        request.getQuestionId()
                )
                .isPresent()) {

            throw new RuntimeException(
                    "Question has already been answered"
            );
        }

        Question question =
                questionRepository.findById(
                                request.getQuestionId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Question not found"
                                )
                        );

        double score = 0.0;

        double maxScore =
                question.getMaxScore() != null
                        ? question.getMaxScore()
                        : 100.0;

        boolean correct = false;

        String feedback = null;

        List<String> matchedKeywords =
                new ArrayList<>();

        String correctAnswerText = null;

        if (question.getType() ==
                QuestionType.MCQ) {

            Integer selectedIndex =
                    request.getSelectedOptionIndex();

            int correctIndex =
                    question.getCorrectIndex();

            if (question.getOptions() != null &&
                    correctIndex >= 0 &&
                    correctIndex < question.getOptions().size()) {

                correctAnswerText =
                        question.getOptions()
                                .get(correctIndex);
            }

            if (selectedIndex != null &&
                    selectedIndex == correctIndex) {

                score = maxScore;
                correct = true;
                feedback =
                        "Correct answer.";

            } else {

                score = 0.0;
                correct = false;
                feedback =
                        "Incorrect answer.";
            }
        }

        else {

            correctAnswerText =
                    question.getExpectedAnswer();

            if (request.getUserAnswer() == null ||
                    request.getUserAnswer().isBlank()) {

                score = 0.0;
                correct = false;

                feedback =
                        "No answer was provided.";

            } else {

                AnswerEvaluationResponse evaluation =
                        geminiService.evaluateAnswer(
                                question.getQuestionText(),
                                question.getExpectedAnswer(),
                                question.getIdealKeywords(),
                                request.getUserAnswer()
                        );

                if (evaluation != null) {

                    score =
                            evaluation.getScore();

                    feedback =
                            evaluation.getFeedback();

                    matchedKeywords =
                            evaluation.getMatchedKeywords();

                    correct =
                            score >= 50.0;
                }
            }
        }

        if (score < 0.0) {
            score = 0.0;
        }

        if (score > maxScore) {
            score = maxScore;
        }

        InterviewAnswer answer =
                InterviewAnswer.builder()
                        .interviewSessionId(
                                interviewId
                        )
                        .questionId(
                                request.getQuestionId()
                        )
                        .userAnswer(
                                request.getUserAnswer()
                        )
                        .selectedOptionIndex(
                                request.getSelectedOptionIndex()
                        )
                        .score(score)
                        .maxScore(maxScore)
                        .correct(correct)
                        .feedback(feedback)
                        .matchedKeywords(
                                matchedKeywords
                        )
                        .correctAnswer(
                                correctAnswerText
                        )
                        .build();

        answerRepository.save(answer);

        int answeredQuestions =
                session.getAnsweredQuestions() != null
                        ? session.getAnsweredQuestions()
                        : 0;

        int correctAnswers =
                session.getCorrectAnswers() != null
                        ? session.getCorrectAnswers()
                        : 0;

        int incorrectAnswers =
                session.getIncorrectAnswers() != null
                        ? session.getIncorrectAnswers()
                        : 0;

        double totalScore =
                session.getTotalScore() != null
                        ? session.getTotalScore()
                        : 0.0;

        answeredQuestions++;

        if (correct) {
            correctAnswers++;
        } else {
            incorrectAnswers++;
        }

        totalScore += score;

        double currentPercentage = 0.0;

        if (answeredQuestions > 0) {

            currentPercentage =
                    (
                            totalScore /
                                    (answeredQuestions * 100.0)
                    ) * 100.0;
        }

        currentPercentage =
                Math.max(
                        0.0,
                        Math.min(
                                100.0,
                                currentPercentage
                        )
                );

        session.setAnsweredQuestions(
                answeredQuestions
        );

        session.setCorrectAnswers(
                correctAnswers
        );

        session.setIncorrectAnswers(
                incorrectAnswers
        );

        session.setTotalScore(
                totalScore
        );

        session.setPercentage(
                currentPercentage
        );

        interviewRepository.save(session);

        int totalQuestions =
                session.getNumberOfQuestions();

        int remainingQuestions =
                Math.max(
                        0,
                        totalQuestions -
                                answeredQuestions
                );

        return SubmitAnswerResponse
                .builder()
                .questionId(
                        request.getQuestionId()
                )
                .score(score)
                .maxScore(maxScore)
                .correct(correct)
                .feedback(feedback)
                .matchedKeywords(
                        matchedKeywords
                )
                .answeredQuestions(
                        answeredQuestions
                )
                .remainingQuestions(
                        remainingQuestions
                )
                .currentScore(
                        totalScore
                )
                .currentPercentage(
                        currentPercentage
                )
                .correctAnswer(
                        correctAnswerText
                )
                .build();
    }

    public Interview completeInterview(
            String interviewId,
            String userId
    ) {

        Interview session =
                interviewRepository
                        .findByIdAndUserId(
                                interviewId,
                                userId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Interview not found"
                                )
                        );

        if (session.getStatus() !=
                InterviewStatus.IN_PROGRESS) {

            throw new RuntimeException(
                    "Interview is not in progress"
            );
        }

        session.setStatus(
                InterviewStatus.COMPLETED
        );

        session.setEndTime(
                LocalDateTime.now()
        );

        if (session.getStartTime() != null) {

            session.setDurationSeconds(
                    Duration.between(
                            session.getStartTime(),
                            session.getEndTime()
                    ).getSeconds()
            );
        }

        int totalQuestions =
                session.getNumberOfQuestions() != null
                        ? session.getNumberOfQuestions()
                        : 0;

        int answeredQuestions =
                session.getAnsweredQuestions() != null
                        ? session.getAnsweredQuestions()
                        : 0;

        session.setSkippedQuestions(
                Math.max(
                        0,
                        totalQuestions -
                                answeredQuestions
                )
        );

        double totalScore =
                session.getTotalScore() != null
                        ? session.getTotalScore()
                        : 0.0;

        double finalPercentage = 0.0;

        if (totalQuestions > 0) {

            finalPercentage =
                    (
                            totalScore /
                                    (totalQuestions * 100.0)
                    ) * 100.0;
        }

        finalPercentage =
                Math.max(
                        0.0,
                        Math.min(
                                100.0,
                                finalPercentage
                        )
                );

        session.setPercentage(
                finalPercentage
        );

        try {

            List<InterviewAnswer> answers =
                    answerRepository
                            .findByInterviewSessionId(
                                    interviewId
                            );

            List<String> performanceData =
                    new ArrayList<>();

            if (answers != null) {

                for (InterviewAnswer answer :
                        answers) {

                    Question question =
                            questionRepository
                                    .findById(
                                            answer.getQuestionId()
                                    )
                                    .orElse(null);

                    if (question == null) {
                        continue;
                    }

                    StringBuilder data =
                            new StringBuilder();

                    data.append(
                            "Question: "
                    );

                    data.append(
                            question.getQuestionText()
                    );

                    data.append(
                            "\nScore: "
                    );

                    data.append(
                            answer.getScore() != null
                                    ? answer.getScore()
                                    : 0.0
                    );

                    data.append(
                            "/ "
                    );

                    data.append(
                            answer.getMaxScore() != null
                                    ? answer.getMaxScore()
                                    : 100.0
                    );

                    data.append(
                            "\nCorrect: "
                    );

                    data.append(
                            Boolean.TRUE.equals(
                                    answer.getCorrect()
                            )
                    );

                    if (answer.getFeedback() != null) {

                        data.append(
                                "\nFeedback: "
                        );

                        data.append(
                                answer.getFeedback()
                        );
                    }

                    if (question.getIdealKeywords() !=
                            null &&
                            !question.getIdealKeywords()
                                    .isEmpty()) {

                        data.append(
                                "\nExpected Topics/Keywords: "
                        );

                        data.append(
                                String.join(
                                        ", ",
                                        question
                                                .getIdealKeywords()
                                )
                        );
                    }

                    performanceData.add(
                            data.toString()
                    );
                }
            }

            String companyName =
                    "Unknown Company";

            if (session.getCompanyId() != null) {

                Company company =
                        companyRepository
                                .findById(
                                        session.getCompanyId()
                                )
                                .orElse(null);

                if (company != null &&
                        company.getName() != null) {

                    companyName =
                            company.getName();
                }
            }

            String categoryName =
                    getCategoryDisplayName(
                            session
                    );

            var analysis =
                    geminiService
                            .generatePerformanceAnalysis(
                                    companyName,
                                    categoryName,
                                    session.getDifficulty(),
                                    finalPercentage,
                                    performanceData
                            );

            if (analysis != null) {

                session.setAiFeedback(
                        analysis.getAiFeedback()
                );

                session.setStrongTopics(
                        analysis.getStrongTopics()
                );

                session.setWeakTopics(
                        analysis.getWeakTopics()
                );

                session.setAiRecommendation(
                        analysis.getAiRecommendation()
                );

                session.setAiAnalysisGeneratedAt(
                        LocalDateTime.now()
                );
            }

        } catch (Exception e) {

            session.setAiFeedback(
                    "Your interview has been completed. Review your answers and focus on the areas where you lost marks."
            );

            session.setStrongTopics(
                    new ArrayList<>()
            );

            session.setWeakTopics(
                    new ArrayList<>()
            );

            session.setAiRecommendation(
                    "Practice the topics associated with your lower-scoring answers before attempting another interview."
            );

            session.setAiAnalysisGeneratedAt(
                    LocalDateTime.now()
            );
        }

        return interviewRepository.save(
                session
        );
    }

    private String getCategoryDisplayName(
            Interview session
    ) {

        if (session.getCategoryId() != null &&
                !session.getCategoryId().isBlank()) {

            Category category =
                    categoryRepository
                            .findById(
                                    session.getCategoryId()
                            )
                            .orElse(null);

            if (category != null &&
                    category.getName() != null) {

                return category.getName();
            }
        }

        if (session.getInterviewMode() == null) {
            return "General Interview";
        }

        return switch (
                session.getInterviewMode()
                ) {

            case HR ->
                    "HR / Human Resources";

            case APTITUDE ->
                    "Aptitude";

            case MIXED ->
                    "Mixed Interview";

            case TECHNICAL ->
                    "Technical Interview";

            case PRACTICE ->
                    "Personalized Practice";
        };
    }

    public Page<InterviewHistoryResponse> getHistory(
            String userId,
            Pageable pageable
    ) {

        return interviewRepository
                .findByUserIdOrderByStartTimeDesc(
                        userId,
                        pageable
                )
                .map(
                        i -> {

                            Company company = null;

                            if (i.getCompanyId() != null) {

                                company =
                                        companyRepository
                                                .findById(
                                                        i.getCompanyId()
                                                )
                                                .orElse(null);
                            }

                            Category category = null;

                            if (i.getCategoryId() != null) {

                                category =
                                        categoryRepository
                                                .findById(
                                                        i.getCategoryId()
                                                )
                                                .orElse(null);
                            }

                            return InterviewHistoryResponse
                                    .builder()
                                    .interviewId(
                                            i.getId()
                                    )
                                    .companyName(
                                            company != null
                                                    ? company.getName()
                                                    : "Unknown"
                                    )
                                    .categoryName(
                                            category != null
                                                    ? category.getName()
                                                    : getHistoryCategoryName(
                                                    i
                                            )
                                    )
                                    .difficulty(
                                            i.getDifficulty()
                                    )
                                    .questionType(
                                            i.getQuestionType()
                                    )
                                    .totalQuestions(
                                            i.getNumberOfQuestions()
                                    )
                                    .percentage(
                                            i.getPercentage()
                                    )
                                    .correctAnswers(
                                            i.getCorrectAnswers()
                                    )
                                    .completedAt(
                                            i.getEndTime()
                                    )
                                    .status(
                                            i.getStatus()
                                    )
                                    .build();
                        }
                );
    }

    private String getHistoryCategoryName(
            Interview interview
    ) {

        if (interview.getInterviewMode() == null) {
            return "General";
        }

        return switch (
                interview.getInterviewMode()
                ) {

            case HR ->
                    "HR";

            case APTITUDE ->
                    "Aptitude";

            case MIXED ->
                    "Mixed";

            case TECHNICAL ->
                    "Technical";

            case PRACTICE ->
                    "Practice";
        };
    }

    public InterviewDetailsResponse getInterviewDetails(
            String interviewId,
            String userId
    ) {

        Interview session =
                interviewRepository
                        .findByIdAndUserId(
                                interviewId,
                                userId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Interview not found"
                                )
                        );

        List<InterviewAnswer> answers =
                answerRepository
                        .findByInterviewSessionId(
                                interviewId
                        );

        if (answers != null) {

            for (InterviewAnswer answer : answers) {

                if (answer.getQuestionId() == null) {
                    continue;
                }

                questionRepository
                        .findById(answer.getQuestionId())
                        .ifPresent(
                                q -> answer.setQuestionText(
                                        q.getQuestionText()
                                )
                        );
            }
        }

        String companyName = "Unknown Company";

        if (session.getCompanyId() != null) {

            Company company =
                    companyRepository
                            .findById(session.getCompanyId())
                            .orElse(null);

            if (company != null && company.getName() != null) {
                companyName = company.getName();
            }
        }

        String categoryName =
                getCategoryDisplayName(session);

        return InterviewDetailsResponse
                .builder()
                .session(session)
                .answers(answers)
                .companyName(companyName)
                .categoryName(categoryName)
                .interviewMode(session.getInterviewMode())
                .difficulty(session.getDifficulty())
                .build();
    }

    public StartInterviewResponse startPractice(
            String userId
    ) {

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        List<Interview> recentCompleted =
                interviewRepository
                        .findByUserIdAndStatusOrderByEndTimeDesc(
                                userId,
                                InterviewStatus.COMPLETED
                        )
                        .stream()
                        .limit(10)
                        .toList();

        List<String> focusTopics =
                aggregateTopics(
                        recentCompleted,
                        Interview::getWeakTopics
                );

        List<String> strongFallbackTopics =
                focusTopics.isEmpty()
                        ? aggregateTopics(
                        recentCompleted,
                        Interview::getStrongTopics
                )
                        : List.of();

        boolean isStretchPractice =
                focusTopics.isEmpty() &&
                        !strongFallbackTopics.isEmpty();

        String companyId =
                recentCompleted.isEmpty()
                        ? null
                        : recentCompleted.get(0).getCompanyId();

        Company company =
                companyId != null
                        ? companyRepository.findById(companyId)
                        .orElse(null)
                        : null;

        if (company == null) {

            List<Company> activeCompanies =
                    companyRepository.findByActiveTrue();

            company =
                    activeCompanies.isEmpty()
                            ? null
                            : activeCompanies.get(0);
        }

        if (company == null) {

            throw new RuntimeException(
                    "No company is configured yet - add one from the admin console before starting practice"
            );
        }

        Difficulty lastDifficulty =
                recentCompleted.isEmpty() ||
                        recentCompleted.get(0).getDifficulty() == null
                        ? Difficulty.MEDIUM
                        : recentCompleted.get(0).getDifficulty();

        Difficulty practiceDifficulty =
                isStretchPractice
                        ? stepUpDifficulty(lastDifficulty)
                        : lastDifficulty;

        int topicCount =
                Math.max(
                        focusTopics.size(),
                        strongFallbackTopics.size()
                );

        int numberOfQuestions =
                topicCount == 0
                        ? 5
                        : Math.min(10, Math.max(5, topicCount * 2));

        QuestionType questionType = QuestionType.MCQ;

        List<Question> practiceQuestions =
                geminiService.generatePracticeQuestions(
                        company.getId(),
                        focusTopics,
                        strongFallbackTopics,
                        practiceDifficulty,
                        questionType,
                        numberOfQuestions
                );

        if (practiceQuestions == null ||
                practiceQuestions.isEmpty()) {

            throw new RuntimeException(
                    "Could not generate practice questions right now - please try again"
            );
        }

        List<String> questionIds =
                practiceQuestions.stream()
                        .map(Question::getId)
                        .collect(Collectors.toList());

        Interview session =
                Interview.builder()
                        .userId(userId)
                        .companyId(company.getId())
                        .categoryId(null)
                        .interviewMode(InterviewMode.PRACTICE)
                        .difficulty(practiceDifficulty)
                        .questionType(questionType)
                        .numberOfQuestions(practiceQuestions.size())
                        .questionIds(questionIds)
                        .answeredQuestions(0)
                        .correctAnswers(0)
                        .incorrectAnswers(0)
                        .skippedQuestions(0)
                        .totalScore(0.0)
                        .percentage(0.0)
                        .status(InterviewStatus.IN_PROGRESS)
                        .startTime(LocalDateTime.now())
                        .build();

        session = interviewRepository.save(session);

        return StartInterviewResponse
                .builder()
                .interviewId(session.getId())
                .companyId(session.getCompanyId())
                .categoryId(session.getCategoryId())
                .difficulty(session.getDifficulty())
                .questionType(session.getQuestionType())
                .totalQuestions(session.getNumberOfQuestions())
                .questions(
                        practiceQuestions.stream()
                                .map(
                                        q ->
                                                QuestionForInterviewResponse
                                                        .builder()
                                                        .id(q.getId())
                                                        .questionText(q.getQuestionText())
                                                        .type(q.getType())
                                                        .difficulty(q.getDifficulty())
                                                        .categoryId(q.getCategoryId())
                                                        .companyIds(q.getCompanyIds())
                                                        .options(q.getOptions())
                                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .startedAt(session.getStartTime())
                .status(session.getStatus())
                .practiceMode(true)
                .focusTopics(
                        isStretchPractice
                                ? strongFallbackTopics
                                : focusTopics
                )
                .build();
    }

    private List<String> aggregateTopics(
            List<Interview> interviews,
            java.util.function.Function<Interview, List<String>> topicsOf
    ) {

        java.util.LinkedHashMap<String, String> firstSeenCasing =
                new java.util.LinkedHashMap<>();

        java.util.Map<String, Integer> frequency =
                new java.util.HashMap<>();

        for (Interview interview : interviews) {

            List<String> topics = topicsOf.apply(interview);

            if (topics == null) {
                continue;
            }

            for (String topic : topics) {

                if (topic == null || topic.isBlank()) {
                    continue;
                }

                String key = topic.trim().toLowerCase();

                firstSeenCasing.putIfAbsent(key, topic.trim());
                frequency.merge(key, 1, Integer::sum);
            }
        }

        return frequency.entrySet().stream()
                .sorted(
                        (a, b) -> b.getValue() - a.getValue()
                )
                .limit(6)
                .map(e -> firstSeenCasing.get(e.getKey()))
                .collect(Collectors.toList());
    }

    private Difficulty stepUpDifficulty(Difficulty difficulty) {

        if (difficulty == Difficulty.EASY) {
            return Difficulty.MEDIUM;
        }

        if (difficulty == Difficulty.MEDIUM) {
            return Difficulty.HARD;
        }

        return Difficulty.HARD;
    }

    public ProgressResponse getProgress(
            String userId
    ) {

        List<Interview> allInterviews =
                interviewRepository
                        .findByUserIdOrderByStartTimeDesc(
                                userId,
                                Pageable.unpaged()
                        )
                        .getContent();

        List<Interview> completed =
                allInterviews.stream()
                        .filter(
                                i ->
                                        i.getStatus()
                                                ==
                                                InterviewStatus.COMPLETED
                        )
                        .toList();

        long totalInterviews =
                allInterviews.size();

        long completedInterviews =
                completed.size();

        if (completedInterviews == 0) {

            return new ProgressResponse(
                    totalInterviews,
                    0L,
                    0.0,
                    0.0,
                    0L,
                    0L,
                    0L,
                    0.0
            );
        }

        double totalScore =
                completed.stream()
                        .mapToDouble(
                                i ->
                                        i.getPercentage() != null
                                                ? i.getPercentage()
                                                : 0.0
                        )
                        .sum();

        double highestScore =
                completed.stream()
                        .mapToDouble(
                                i ->
                                        i.getPercentage() != null
                                                ? i.getPercentage()
                                                : 0.0
                        )
                        .max()
                        .orElse(0.0);

        long totalQuestionsAttempted =
                completed.stream()
                        .mapToLong(
                                i ->
                                        i.getAnsweredQuestions() != null
                                                ? i.getAnsweredQuestions()
                                                : 0
                        )
                        .sum();

        long totalCorrectAnswers =
                completed.stream()
                        .mapToLong(
                                i ->
                                        i.getCorrectAnswers() != null
                                                ? i.getCorrectAnswers()
                                                : 0
                        )
                        .sum();

        long totalIncorrectAnswers =
                completed.stream()
                        .mapToLong(
                                i ->
                                        i.getIncorrectAnswers() != null
                                                ? i.getIncorrectAnswers()
                                                : 0
                        )
                        .sum();

        double averagePercentage =
                completed.stream()
                        .mapToDouble(
                                i ->
                                        i.getPercentage() != null
                                                ? i.getPercentage()
                                                : 0.0
                        )
                        .average()
                        .orElse(0.0);

        return new ProgressResponse(
                totalInterviews,
                completedInterviews,
                totalScore /
                        completedInterviews,
                highestScore,
                totalQuestionsAttempted,
                totalCorrectAnswers,
                totalIncorrectAnswers,
                averagePercentage
        );
    }
}