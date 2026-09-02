package com.prepgenius.repository;

import com.prepgenius.model.User;
import com.prepgenius.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);

    Page<User> findAllByRole(UserRole role, Pageable pageable);
}