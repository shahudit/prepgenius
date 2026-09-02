package com.prepgenius.service;

import com.prepgenius.dto.UpdateUserRoleRequest;
import com.prepgenius.dto.UserResponse;
import com.prepgenius.model.User;
import com.prepgenius.model.UserRole;
import com.prepgenius.repository.InterviewRepository;
import com.prepgenius.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;

    public Page<UserResponse> getAllUsers(Pageable pageable) {

        return userRepository
                .findAllByRole(UserRole.USER, pageable)
                .map(this::mapToResponse);
    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    public UserResponse updateUserRole(String id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(request.getRole());
        userRepository.save(user);

        return mapToResponse(user);
    }

    public void deleteUser(String id, String currentAdminEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmail().equals(currentAdminEmail)) {
            throw new RuntimeException("Admins cannot delete themselves");
        }

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .interviewCount(interviewRepository.countByUserId(user.getId()))
                .build();
    }
}