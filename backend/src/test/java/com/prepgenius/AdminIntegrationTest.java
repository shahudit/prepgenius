package com.prepgenius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepgenius.dto.CategoryRequest;
import com.prepgenius.dto.CompanyRequest;
import com.prepgenius.dto.LoginRequest;
import com.prepgenius.dto.UpdateUserRoleRequest;
import com.prepgenius.model.InterviewMode;
import com.prepgenius.model.User;
import com.prepgenius.model.UserRole;
import com.prepgenius.repository.CategoryRepository;
import com.prepgenius.repository.CompanyRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        companyRepository.deleteAll();
        categoryRepository.deleteAll();

        adminUser = User.builder()
                .name("Admin")
                .email("admin@test.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(adminUser);

        regularUser = User.builder()
                .name("User")
                .email("user@test.com")
                .passwordHash(passwordEncoder.encode("user123"))
                .role(UserRole.USER)
                .build();
        userRepository.save(regularUser);

        adminToken = getToken("admin@test.com", "admin123");
        userToken = getToken("user@test.com", "user123");
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
    void testAdminCanGetUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void testUserCannotGetUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanUpdateRole() throws Exception {
        UpdateUserRoleRequest request = UpdateUserRoleRequest.builder()
                .role(UserRole.ADMIN)
                .build();

        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testAdminCannotDeleteSelf() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Admins cannot delete themselves"));
    }

    @Test
    void testAdminCompanyCRUD() throws Exception {
        CompanyRequest request = CompanyRequest.builder()
                .name("TCS")
                .focus("IT Services")
                .build();

        MvcResult result = mockMvc.perform(post("/api/admin/companies")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/admin/companies/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TCS"));

        mockMvc.perform(delete("/api/admin/companies/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testAdminCategoryCRUD() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Technical")
                .group(InterviewMode.TECHNICAL)
                .build();

        MvcResult result = mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/admin/categories/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
