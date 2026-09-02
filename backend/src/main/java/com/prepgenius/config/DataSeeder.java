package com.prepgenius.config;

import com.prepgenius.model.Category;
import com.prepgenius.model.Company;
import com.prepgenius.model.InterviewMode;
import com.prepgenius.model.User;
import com.prepgenius.model.UserRole;
import com.prepgenius.repository.CategoryRepository;
import com.prepgenius.repository.CompanyRepository;
import com.prepgenius.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email}")
    private String defaultAdminEmail;

    @Value("${admin.default.password}")
    private String defaultAdminPassword;

    @Value("${admin.default.name}")
    private String defaultAdminName;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(defaultAdminEmail).isEmpty()) {
            User admin = User.builder()
                    .name(defaultAdminName)
                    .email(defaultAdminEmail)
                    .passwordHash(passwordEncoder.encode(defaultAdminPassword))
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Seeded default admin account -> email: {}, password: {}", defaultAdminEmail, defaultAdminPassword);
            log.info("Change this password after first login (ADMIN_EMAIL / ADMIN_PASSWORD env vars to customize).");
        }

        if (companyRepository.count() == 0) {
            companyRepository.saveAll(List.of(
                    Company.builder().name("Google").focus("Algorithms, System Design, Googliness").active(true).build(),
                    Company.builder().name("Amazon").focus("Leadership Principles, DSA, System Design").active(true).build(),
                    Company.builder().name("Microsoft").focus("DSA, System Design, Behavioral").active(true).build(),
                    Company.builder().name("TCS").focus("Aptitude, Core CS, HR").active(true).build(),
                    Company.builder().name("Infosys").focus("Aptitude, Core CS, HR").active(true).build(),
                    Company.builder().name("Accenture").focus("Aptitude, Communication, Core CS").active(true).build()
            ));
        }

        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    Category.builder().name("Java").description("Core Java, OOP, Collections").group(InterviewMode.TECHNICAL).active(true).build(),
                    Category.builder().name("Spring Boot").description("REST APIs, DI, Spring ecosystem").group(InterviewMode.TECHNICAL).active(true).build(),
                    Category.builder().name("ReactJS").description("Hooks, state management, rendering").group(InterviewMode.TECHNICAL).active(true).build(),
                    Category.builder().name("Python").description("Core Python, data structures").group(InterviewMode.TECHNICAL).active(true).build(),
                    Category.builder().name("MongoDB").description("Schema design, queries, indexing").group(InterviewMode.TECHNICAL).active(true).build(),
                    Category.builder().name("SQL").description("Joins, normalization, query tuning").group(InterviewMode.TECHNICAL).active(true).build(),
                    Category.builder().name("Aptitude").description("Quantitative and logical reasoning").group(InterviewMode.APTITUDE).active(true).build(),
                    Category.builder().name("HR").description("Behavioral and situational questions").group(InterviewMode.HR).active(true).build()
            ));
        }
    }
}
