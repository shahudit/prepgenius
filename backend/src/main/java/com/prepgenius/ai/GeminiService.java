package com.prepgenius.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepgenius.dto.AiPerformanceAnalysisResponse;
import com.prepgenius.dto.AnswerEvaluationResponse;
import com.prepgenius.dto.StudyMaterialResponse;
import com.prepgenius.dto.TopicStudyMaterial;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final GeminiClient geminiClient;
    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public List<Question> generateAndSaveQuestions(
            String companyId,
            String categoryId,
            Difficulty difficulty,
            QuestionType type,
            int count
    ) {

        return generateAndSaveQuestions(
                companyId,
                categoryId,
                InterviewMode.TECHNICAL,
                difficulty,
                type,
                count
        );
    }

    public List<Question> generateAndSaveQuestions(
            String companyId,
            String categoryId,
            InterviewMode interviewMode,
            Difficulty difficulty,
            QuestionType type,
            int count
    ) {

        Company company =
                companyRepository.findById(companyId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Company not found"
                                )
                        );

        String categoryName;
        String categoryDescription;

        if (interviewMode ==
                InterviewMode.TECHNICAL) {

            if (categoryId == null ||
                    categoryId.isBlank()) {

                throw new RuntimeException(
                        "Technical domain is required"
                );
            }

            Category category =
                    categoryRepository.findById(
                                    categoryId
                            )
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "Technical domain not found"
                                    )
                            );

            categoryName =
                    category.getName();

            categoryDescription =
                    category.getDescription() != null
                            ? category.getDescription()
                            : "";

        } else {

            categoryName =
                    getInterviewCategoryName(
                            interviewMode
                    );

            categoryDescription =
                    getInterviewCategoryDescription(
                            interviewMode
                    );
        }

        String prompt =
                buildQuestionPrompt(
                        company,
                        categoryName,
                        categoryDescription,
                        difficulty,
                        type,
                        count
                );

        log.info(
                "Generating {} {} questions for company={}, mode={}, category={}",
                count,
                type,
                company.getName(),
                interviewMode,
                categoryName
        );

        return generateFromPrompt(
                prompt,
                company.getId(),
                categoryId,
                interviewMode,
                difficulty,
                type,
                count
        );
    }

    public List<Question> generatePracticeQuestions(
            String companyId,
            List<String> focusTopics,
            List<String> strongTopics,
            Difficulty difficulty,
            QuestionType type,
            int count
    ) {

        Company company =
                companyRepository.findById(companyId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Company not found"
                                )
                        );

        String categoryName =
                "Personalized Weak-Topic Practice";

        String categoryDescription =
                buildPracticeDescription(
                        focusTopics,
                        strongTopics
                );

        String prompt =
                buildQuestionPrompt(
                        company,
                        categoryName,
                        categoryDescription,
                        difficulty,
                        type,
                        count
                );

        log.info(
                "Generating {} real-time practice questions for company={}, focusTopics={}",
                count,
                company.getName(),
                focusTopics
        );

        return generateFromPrompt(
                prompt,
                company.getId(),
                null,
                InterviewMode.PRACTICE,
                difficulty,
                type,
                count
        );
    }

    private String buildPracticeDescription(
            List<String> focusTopics,
            List<String> strongTopics
    ) {

        if (focusTopics != null && !focusTopics.isEmpty()) {

            return "This is a personalized practice round generated in "
                    + "real time from the learner's own interview history, "
                    + "across every company and category they have attempted. "
                    + "Write questions that directly and specifically test "
                    + "these weak topics the learner needs to improve: "
                    + String.join(", ", focusTopics)
                    + ". Every question must clearly map to one of these "
                    + "topics so the learner gets targeted, deliberate "
                    + "practice on exactly what they are struggling with. "
                    + "Vary which topic each question covers so all of them "
                    + "get exercised.";
        }

        if (strongTopics != null && !strongTopics.isEmpty()) {

            return "The learner has no recorded weak topics right now - "
                    + "their recent interviews were strong across the board. "
                    + "Write harder, deeper stretch questions that go one "
                    + "level beyond the basics on: "
                    + String.join(", ", strongTopics)
                    + ", so practice keeps challenging them instead of "
                    + "repeating what they already know.";
        }

        return "The learner has no interview history yet, or no scored "
                + "answers to analyze. Write a broad, well-rounded "
                + "diagnostic set of general interview-readiness questions "
                + "covering core problem solving, communication and "
                + "fundamentals, so their strengths and weaknesses can "
                + "start being identified.";
    }

    private List<Question> generateFromPrompt(
            String prompt,
            String companyId,
            String categoryId,
            InterviewMode interviewMode,
            Difficulty difficulty,
            QuestionType type,
            int count
    ) {

        String responseJson =
                geminiClient.generateContent(
                        prompt
                );

        if (responseJson == null ||
                responseJson.isBlank()) {

            throw new RuntimeException(
                    "Gemini returned an empty response"
            );
        }

        try {

            String cleanJson =
                    cleanGeminiResponse(
                            responseJson
                    );

            JsonNode root =
                    objectMapper.readTree(
                            cleanJson
                    );

            JsonNode questionsNode =
                    root.get("questions");

            if (questionsNode == null ||
                    !questionsNode.isArray() ||
                    questionsNode.isEmpty()) {

                throw new RuntimeException(
                        "Gemini returned no questions"
                );
            }

            List<Question> savedQuestions =
                    new ArrayList<>();

            for (JsonNode node :
                    questionsNode) {

                if (savedQuestions.size()
                        >= count) {
                    break;
                }

                String questionText =
                        textValue(
                                node,
                                "questionText"
                        );

                if (questionText == null ||
                        questionText.isBlank()) {

                    continue;
                }

                List<String> options =
                        readStringList(
                                node.get("options")
                        );

                Integer correctIndex =
                        readInteger(
                                node.get(
                                        "correctOptionIndex"
                                )
                        );

                String expectedAnswer =
                        textValue(
                                node,
                                "expectedAnswer"
                        );

                List<String> idealKeywords =
                        readStringList(
                                node.get(
                                        "idealKeywords"
                                )
                        );

                String explanation =
                        textValue(
                                node,
                                "explanation"
                        );

                if (type == QuestionType.MCQ) {

                    if (options.size() != 4) {
                        log.warn(
                                "Skipping invalid MCQ: expected 4 options"
                        );
                        continue;
                    }

                    if (correctIndex == null ||
                            correctIndex < 0 ||
                            correctIndex >= options.size()) {

                        log.warn(
                                "Skipping MCQ with invalid correct index"
                        );
                        continue;
                    }
                }

                if (type == QuestionType.TEXT) {

                    if (expectedAnswer == null ||
                            expectedAnswer.isBlank()) {

                        log.warn(
                                "Skipping TEXT question without expected answer"
                        );

                        continue;
                    }
                }

                Question question =
                        Question.builder()
                                .questionText(
                                        questionText
                                )
                                .type(type)
                                .difficulty(
                                        difficulty
                                )

                                .categoryId(
                                        categoryId
                                )

                                .interviewMode(
                                        interviewMode
                                )

                                .companyIds(
                                        List.of(companyId)
                                )

                                .options(
                                        type ==
                                                QuestionType.MCQ
                                                ? options
                                                : Collections.emptyList()
                                )

                                .correctIndex(
                                        type ==
                                                QuestionType.MCQ
                                                ? correctIndex
                                                : null
                                )

                                .expectedAnswer(
                                        expectedAnswer
                                )

                                .idealKeywords(
                                        idealKeywords
                                )

                                .explanation(
                                        explanation
                                )

                                .evaluationCriteria(
                                        "Evaluate correctness, relevance, completeness and understanding."
                                )

                                .maxScore(100)

                                .status(
                                        QuestionStatus.AI_GENERATED
                                )

                                .sourceType(
                                        "AI_GENERATED"
                                )

                                .sourceReference(
                                        "Gemini AI"
                                )

                                .build();

                Question saved =
                        questionRepository.save(
                                question
                        );

                savedQuestions.add(saved);
            }

            if (savedQuestions.isEmpty()) {

                throw new RuntimeException(
                        "Gemini generated questions, but none passed validation"
                );
            }

            log.info(
                    "Successfully generated and saved {} questions",
                    savedQuestions.size()
            );

            return savedQuestions;

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Failed to parse Gemini question response",
                    e
            );

            throw new RuntimeException(
                    "Failed to process AI-generated questions.",
                    e
            );
        }
    }

    private String buildQuestionPrompt(
            Company company,
            String categoryName,
            String categoryDescription,
            Difficulty difficulty,
            QuestionType type,
            int count
    ) {

        String questionTypeInstruction;

        if (type == QuestionType.MCQ) {

            questionTypeInstruction =
                    """
                    Each question must be an MCQ.

                    Requirements:
                    - Exactly 4 options.
                    - Only one option is correct.
                    - correctOptionIndex must be 0, 1, 2 or 3.
                    - Options must be plausible.
                    - Include an explanation.
                    """;

        } else {

            questionTypeInstruction =
                    """
                    Each question must be a descriptive/text answer question.

                    Requirements:
                    - Include expectedAnswer.
                    - Include 4 to 6 idealKeywords.
                    - Include an explanation.
                    - options must be [].
                    - correctOptionIndex must be null.
                    """;
        }

        return String.format(
                """
                You are an expert interview preparation assistant.

                Generate exactly %d high-quality interview questions.

                COMPANY:
                %s

                INTERVIEW DOMAIN:
                %s

                DOMAIN DESCRIPTION:
                %s

                DIFFICULTY:
                %s

                QUESTION TYPE:
                %s

                %s

                IMPORTANT:
                - Questions must be relevant to the selected company.
                - Questions must be suitable for real interview preparation.
                - Do not invent confidential company information.
                - Keep questions technically accurate.
                - Avoid duplicates.
                - Return ONLY valid JSON.
                - Do not return markdown.
                - Do not return ```json.

                Required JSON structure:

                {
                  "questions": [
                    {
                      "questionText": "Question text",
                      "options": [
                        "Option 1",
                        "Option 2",
                        "Option 3",
                        "Option 4"
                      ],
                      "correctOptionIndex": 0,
                      "expectedAnswer": "Expected answer",
                      "idealKeywords": [
                        "keyword1",
                        "keyword2",
                        "keyword3",
                        "keyword4"
                      ],
                      "explanation": "Explanation"
                    }
                  ]
                }

                Generate exactly %d questions.
                """,
                count,
                company.getName(),
                categoryName,
                categoryDescription,
                difficulty,
                type,
                questionTypeInstruction,
                count
        );
    }

    public AnswerEvaluationResponse evaluateAnswer(
            String questionText,
            String expectedAnswer,
            List<String> idealKeywords,
            String userAnswer
    ) {

        String prompt =
                String.format(
                        """
                        You are an expert interview evaluator.

                        QUESTION:
                        %s

                        EXPECTED ANSWER:
                        %s

                        IDEAL KEYWORDS:
                        %s

                        CANDIDATE ANSWER:
                        %s

                        Evaluate based on:

                        1. Technical correctness
                        2. Relevance
                        3. Completeness
                        4. Understanding
                        5. Keyword coverage

                        Give a score from 0 to 100.

                        Score rules:
                        0-20 = Completely incorrect
                        21-40 = Poor
                        41-60 = Basic
                        61-80 = Good
                        81-100 = Excellent

                        Return ONLY valid JSON:

                        {
                          "score": 75,
                          "feedback": "Professional feedback",
                          "matchedKeywords": [
                            "keyword1"
                          ]
                        }

                        Do not return markdown.
                        Do not return ```json.
                        """,
                        questionText,
                        expectedAnswer,
                        idealKeywords,
                        userAnswer
                );

        try {

            String responseJson =
                    geminiClient.generateContent(
                            prompt
                    );

            String cleanJson =
                    cleanGeminiResponse(
                            responseJson
                    );

            JsonNode root =
                    objectMapper.readTree(
                            cleanJson
                    );

            int score =
                    root.has("score")
                            ? root.get("score").asInt()
                            : 0;

            score =
                    Math.max(
                            0,
                            Math.min(
                                    100,
                                    score
                            )
                    );

            String feedback =
                    textValue(
                            root,
                            "feedback"
                    );

            List<String> matchedKeywords =
                    readStringList(
                            root.get(
                                    "matchedKeywords"
                            )
                    );

            return AnswerEvaluationResponse
                    .builder()
                    .score(score)
                    .feedback(
                            feedback != null
                                    ? feedback
                                    : "Answer evaluated."
                    )
                    .matchedKeywords(
                            matchedKeywords
                    )
                    .build();

        } catch (Exception e) {

            log.error(
                    "Failed to evaluate descriptive answer",
                    e
            );

            throw new RuntimeException(
                    "Failed to process AI evaluation response.",
                    e
            );
        }
    }

    public AiPerformanceAnalysisResponse generatePerformanceAnalysis(
            String companyName,
            String categoryName,
            Difficulty difficulty,
            Double finalScore,
            List<String> questionData
    ) {

        String questionsText =
                questionData == null ||
                        questionData.isEmpty()
                        ? "No detailed question data available."
                        : String.join(
                        "\n\n",
                        questionData
                );

        String prompt =
                String.format(
                        """
                        You are an expert interview performance analyst.

                        Analyze the candidate's complete interview performance.

                        COMPANY:
                        %s

                        CATEGORY:
                        %s

                        DIFFICULTY:
                        %s

                        FINAL SCORE:
                        %.2f / 100

                        QUESTION PERFORMANCE DATA:
                        %s

                        Identify:

                        1. Overall performance.
                        2. Strong topics.
                        3. Weak topics.
                        4. Specific improvement recommendations.

                        IMPORTANT:
                        - Strong topics must be actual topics from the questions.
                        - Weak topics must be actual topics from the questions.
                        - Do not invent unrelated topics.
                        - Base the analysis on the score and answer performance.
                        - If score is low, clearly identify improvement areas.
                        - If score is high, still identify areas for refinement.

                        Return ONLY valid JSON:

                        {
                          "aiFeedback": "2 to 4 sentence performance summary.",
                          "strongTopics": [
                            "Topic 1",
                            "Topic 2"
                          ],
                          "weakTopics": [
                            "Topic 1",
                            "Topic 2"
                          ],
                          "aiRecommendation": "Specific study and practice recommendation."
                        }

                        Return JSON only.
                        Do not return markdown.
                        Do not return ```json.
                        """,
                        companyName,
                        categoryName,
                        difficulty,
                        finalScore != null
                                ? finalScore
                                : 0.0,
                        questionsText
                );

        try {

            String responseJson =
                    geminiClient.generateContent(
                            prompt
                    );

            String cleanJson =
                    cleanGeminiResponse(
                            responseJson
                    );

            JsonNode root =
                    objectMapper.readTree(
                            cleanJson
                    );

            String feedback =
                    textValue(
                            root,
                            "aiFeedback"
                    );

            List<String> strongTopics =
                    readStringList(
                            root.get(
                                    "strongTopics"
                            )
                    );

            List<String> weakTopics =
                    readStringList(
                            root.get(
                                    "weakTopics"
                            )
                    );

            String recommendation =
                    textValue(
                            root,
                            "aiRecommendation"
                    );

            if (feedback == null ||
                    feedback.isBlank()) {

                feedback =
                        "Your interview has been evaluated based on your answers and score.";
            }

            if (recommendation == null ||
                    recommendation.isBlank()) {

                recommendation =
                        "Review your lower-scoring answers and practice those topics before your next interview.";
            }

            return AiPerformanceAnalysisResponse
                    .builder()
                    .aiFeedback(feedback)
                    .strongTopics(strongTopics)
                    .weakTopics(weakTopics)
                    .aiRecommendation(recommendation)
                    .build();

        } catch (Exception e) {

            log.error(
                    "Failed to generate performance analysis",
                    e
            );

            return AiPerformanceAnalysisResponse
                    .builder()
                    .aiFeedback(
                            "Your interview has been completed. Review your answers and focus on the areas where you lost marks."
                    )
                    .strongTopics(
                            new ArrayList<>()
                    )
                    .weakTopics(
                            new ArrayList<>()
                    )
                    .aiRecommendation(
                            "Practice the topics associated with your lower-scoring answers before attempting another interview."
                    )
                    .build();
        }
    }

    public StudyMaterialResponse generateStudyMaterial(
            List<String> weakTopics,
            String companyName,
            String categoryName
    ) {

        if (weakTopics == null || weakTopics.isEmpty()) {

            return StudyMaterialResponse
                    .builder()
                    .topics(new ArrayList<>())
                    .build();
        }

        String topicsList =
                String.join(", ", weakTopics);

        String prompt =
                String.format(
                        """
                        You are an expert technical interview coach writing
                        an in-depth study guide for a candidate preparing
                        for a "%s" interview at "%s".

                        The candidate was identified as weak in EXACTLY
                        these topics (write about every single one, in
                        this order, and do not add extra topics):

                        %s

                        For EACH topic, write real, specific, in-depth
                        study material - not generic filler. Assume the
                        candidate already sat the interview and needs to
                        actually learn the material before trying again.

                        Return ONLY valid JSON in this exact shape:

                        {
                          "topics": [
                            {
                              "topic": "Exact topic name",
                              "summary": "3 to 5 sentence in-depth explanation of the concept and why it matters in interviews.",
                              "keyConcepts": [
                                "Specific key concept 1",
                                "Specific key concept 2",
                                "Specific key concept 3"
                              ],
                              "examples": [
                                "Concrete example or short code/scenario illustrating the concept"
                              ],
                              "commonMistakes": [
                                "A specific mistake candidates make with this topic"
                              ],
                              "practiceTips": [
                                "A concrete, actionable way to practice this topic"
                              ]
                            }
                          ]
                        }

                        Rules:
                        - keyConcepts: 3 to 5 items.
                        - examples: 1 to 3 items.
                        - commonMistakes: 2 to 3 items.
                        - practiceTips: 2 to 3 items.
                        - Every array item must be a complete, specific sentence or code snippet - never a single word.
                        - Do not return markdown.
                        - Do not return ```json.
                        """,
                        categoryName,
                        companyName,
                        topicsList
                );

        try {

            String responseJson =
                    geminiClient.generateContent(
                            prompt
                    );

            String cleanJson =
                    cleanGeminiResponse(
                            responseJson
                    );

            JsonNode root =
                    objectMapper.readTree(
                            cleanJson
                    );

            JsonNode topicsNode =
                    root.get("topics");

            List<TopicStudyMaterial> materials =
                    new ArrayList<>();

            if (topicsNode != null &&
                    topicsNode.isArray()) {

                for (JsonNode node : topicsNode) {

                    String topicName =
                            textValue(
                                    node,
                                    "topic"
                            );

                    if (topicName == null ||
                            topicName.isBlank()) {
                        continue;
                    }

                    materials.add(
                            TopicStudyMaterial
                                    .builder()
                                    .topic(topicName)
                                    .summary(
                                            textValue(
                                                    node,
                                                    "summary"
                                            )
                                    )
                                    .keyConcepts(
                                            readStringList(
                                                    node.get(
                                                            "keyConcepts"
                                                    )
                                            )
                                    )
                                    .examples(
                                            readStringList(
                                                    node.get(
                                                            "examples"
                                                    )
                                            )
                                    )
                                    .commonMistakes(
                                            readStringList(
                                                    node.get(
                                                            "commonMistakes"
                                                    )
                                            )
                                    )
                                    .practiceTips(
                                            readStringList(
                                                    node.get(
                                                            "practiceTips"
                                                    )
                                            )
                                    )
                                    .build()
                    );
                }
            }

            for (String topic : weakTopics) {

                boolean present =
                        materials.stream()
                                .anyMatch(
                                        m -> m.getTopic() != null &&
                                                m.getTopic()
                                                        .equalsIgnoreCase(
                                                                topic
                                                        )
                                );

                if (!present) {

                    materials.add(
                            TopicStudyMaterial
                                    .builder()
                                    .topic(topic)
                                    .summary(
                                            "Focus additional study time on this topic - review core concepts, worked examples, and practice questions before your next interview."
                                    )
                                    .keyConcepts(
                                            new ArrayList<>()
                                    )
                                    .examples(
                                            new ArrayList<>()
                                    )
                                    .commonMistakes(
                                            new ArrayList<>()
                                    )
                                    .practiceTips(
                                            new ArrayList<>()
                                    )
                                    .build()
                    );
                }
            }

            return StudyMaterialResponse
                    .builder()
                    .topics(materials)
                    .build();

        } catch (Exception e) {

            log.error(
                    "Failed to generate weak-topic study material",
                    e
            );

            List<TopicStudyMaterial> fallback =
                    new ArrayList<>();

            for (String topic : weakTopics) {

                fallback.add(
                        TopicStudyMaterial
                                .builder()
                                .topic(topic)
                                .summary(
                                        "In-depth material could not be generated right now. Review your interview feedback for this topic and practice related questions."
                                )
                                .keyConcepts(
                                        new ArrayList<>()
                                )
                                .examples(
                                        new ArrayList<>()
                                )
                                .commonMistakes(
                                        new ArrayList<>()
                                )
                                .practiceTips(
                                        new ArrayList<>()
                                )
                                .build()
                );
            }

            return StudyMaterialResponse
                    .builder()
                    .topics(fallback)
                    .build();
        }
    }

    private String getInterviewCategoryName(
            InterviewMode interviewMode
    ) {

        if (interviewMode == null) {
            return "General Interview";
        }

        return switch (interviewMode) {

            case HR ->
                    "HR / Human Resources Interview";

            case APTITUDE ->
                    "Aptitude Interview";

            case MIXED ->
                    "Mixed Interview";

            case TECHNICAL ->
                    "Technical Interview";

            case PRACTICE ->
                    "Personalized Practice";
        };
    }

    private String getInterviewCategoryDescription(
            InterviewMode interviewMode
    ) {

        if (interviewMode == null) {
            return "General interview preparation.";
        }

        return switch (interviewMode) {

            case HR ->
                    "Behavioral, situational, communication, teamwork, leadership and HR interview questions.";

            case APTITUDE ->
                    "Quantitative aptitude, logical reasoning, numerical reasoning and analytical problem-solving.";

            case MIXED ->
                    "A combination of technical, aptitude, behavioral and interview-oriented questions.";

            case TECHNICAL ->
                    "Technical interview preparation.";

            case PRACTICE ->
                    "Personalized, real-time practice targeted at the learner's own weak (or strong) topics.";
        };
    }

    private String textValue(
            JsonNode node,
            String field
    ) {

        if (node == null ||
                !node.has(field) ||
                node.get(field).isNull()) {

            return null;
        }

        return node.get(field).asText();
    }

    private Integer readInteger(
            JsonNode node
    ) {

        if (node == null ||
                node.isNull() ||
                !node.isNumber()) {

            return null;
        }

        return node.asInt();
    }

    private List<String> readStringList(
            JsonNode node
    ) {

        if (node == null ||
                !node.isArray()) {

            return new ArrayList<>();
        }

        List<String> values =
                new ArrayList<>();

        for (JsonNode item : node) {

            if (item != null &&
                    !item.isNull()) {

                String value =
                        item.asText();

                if (value != null &&
                        !value.isBlank()) {

                    values.add(value);
                }
            }
        }

        return values;
    }

    private String cleanGeminiResponse(
            String response
    ) {

        if (response == null ||
                response.isBlank()) {

            throw new RuntimeException(
                    "Gemini returned an empty response."
            );
        }

        String cleaned =
                response.trim();

        if (cleaned.startsWith("```json")) {

            cleaned =
                    cleaned.substring(7);

        } else if (cleaned.startsWith("```")) {

            cleaned =
                    cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {

            cleaned =
                    cleaned.substring(
                            0,
                            cleaned.length() - 3
                    );
        }

        cleaned =
                cleaned.trim();

        int firstBrace =
                cleaned.indexOf("{");

        int lastBrace =
                cleaned.lastIndexOf("}");

        if (firstBrace >= 0 &&
                lastBrace > firstBrace) {

            cleaned =
                    cleaned.substring(
                            firstBrace,
                            lastBrace + 1
                    );
        }

        return cleaned.trim();
    }
}