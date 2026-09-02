package com.prepgenius.repository;

import com.prepgenius.model.InterviewAnswer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewAnswerRepository
        extends MongoRepository<InterviewAnswer, String> {

    Optional<InterviewAnswer> findByInterviewSessionIdAndQuestionId(
            String interviewSessionId,
            String questionId
    );

    List<InterviewAnswer> findByInterviewSessionId(
            String interviewSessionId
    );
}