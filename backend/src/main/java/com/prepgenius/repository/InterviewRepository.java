package com.prepgenius.repository;

import com.prepgenius.model.Interview;
import com.prepgenius.model.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends MongoRepository<Interview, String> {
    Page<Interview> findByUserIdOrderByStartTimeDesc(String userId, Pageable pageable);
    Optional<Interview> findByIdAndUserId(String id, String userId);
    long countByUserId(String userId);
    List<Interview> findByStatus(InterviewStatus status);

    List<Interview> findByUserIdAndStatusOrderByEndTimeDesc(String userId, InterviewStatus status);
}
