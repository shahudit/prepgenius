package com.prepgenius.service;

import com.prepgenius.dto.AdminDashboardResponse;
import com.prepgenius.dto.AdminReportsResponse;
import com.prepgenius.dto.CategoryStat;
import com.prepgenius.dto.CompanyStat;
import com.prepgenius.dto.RecentInterviewResponse;
import com.prepgenius.dto.TopLearnerStat;
import com.prepgenius.model.Category;
import com.prepgenius.model.Company;
import com.prepgenius.model.Interview;
import com.prepgenius.model.InterviewStatus;
import com.prepgenius.model.User;
import com.prepgenius.model.UserRole;
import com.prepgenius.repository.CategoryRepository;
import com.prepgenius.repository.CompanyRepository;
import com.prepgenius.repository.InterviewRepository;
import com.prepgenius.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportsService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;

    public AdminDashboardResponse getDashboardStats() {

        long totalUsers = userRepository.countByRole(UserRole.USER);

        long totalCompanies = companyRepository.count();
        long totalCategories = categoryRepository.count();

        Map<String, User> users = loadUsers();

        List<Interview> completed = getCompletedInterviews(users);

        double averageScore = calculateAverageScore(completed);

        Map<String, Company> companies = loadCompanies();
        Map<String, Category> categories = loadCategories();

        List<RecentInterviewResponse> recentInterviews = completed.stream()
                .sorted(Comparator.comparing(
                        Interview::getEndTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(6)
                .map(interview -> toRecentInterviewResponse(
                        interview,
                        companies,
                        categories,
                        users
                ))
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCompanies(totalCompanies)
                .totalCategories(totalCategories)
                .totalInterviews(completed.size())
                .averageScore(round(averageScore))
                .recentInterviews(recentInterviews)
                .build();
    }

    public AdminReportsResponse getReports() {

        long totalCompanies = companyRepository.count();
        long totalCategories = categoryRepository.count();

        Map<String, Company> companyCache = loadCompanies();
        Map<String, Category> categoryCache = loadCategories();
        Map<String, User> userCache = loadUsers();

        List<Interview> completed = getCompletedInterviews(userCache);

        Map<String, double[]> categoryAggregation = new LinkedHashMap<>();
        Map<String, Integer> companyAggregation = new LinkedHashMap<>();
        Map<String, double[]> learnerAggregation = new LinkedHashMap<>();

        for (Interview interview : completed) {

            if (interview == null) {
                continue;
            }

            if (interview.getUserId() == null ||
                    interview.getUserId().isBlank() ||
                    !userCache.containsKey(interview.getUserId())) {
                continue;
            }

            double percentage =
                    sanitizePercentage(interview.getPercentage());

            String categoryName = resolveCategoryName(
                    interview.getCategoryId(),
                    categoryCache
            );

            double[] categoryStats =
                    categoryAggregation.computeIfAbsent(
                            categoryName,
                            key -> new double[]{0.0, 0.0}
                    );

            categoryStats[0] += percentage;
            categoryStats[1] += 1.0;

            String companyName = resolveCompanyName(
                    interview.getCompanyId(),
                    companyCache
            );

            companyAggregation.merge(
                    companyName,
                    1,
                    Integer::sum
            );

            double[] learnerStats =
                    learnerAggregation.computeIfAbsent(
                            interview.getUserId(),
                            key -> new double[]{0.0, 0.0}
                    );

            learnerStats[0] += percentage;
            learnerStats[1] += 1.0;
        }

        List<CategoryStat> byCategory =
                categoryAggregation.entrySet()
                        .stream()
                        .map(entry -> {

                            double[] values = entry.getValue();

                            int average = values[1] > 0
                                    ? (int) Math.round(
                                    values[0] / values[1]
                            )
                                    : 0;

                            return CategoryStat.builder()
                                    .name(entry.getKey())
                                    .avg(average)
                                    .attempts((int) values[1])
                                    .build();
                        })
                        .sorted(
                                Comparator.comparing(
                                        CategoryStat::getAttempts,
                                        Comparator.reverseOrder()
                                )
                        )
                        .collect(Collectors.toList());

        List<CompanyStat> byCompany =
                companyAggregation.entrySet()
                        .stream()
                        .map(entry ->
                                CompanyStat.builder()
                                        .name(entry.getKey())
                                        .value(entry.getValue())
                                        .build()
                        )
                        .sorted(
                                Comparator.comparing(
                                        CompanyStat::getValue,
                                        Comparator.reverseOrder()
                                )
                        )
                        .collect(Collectors.toList());

        List<TopLearnerStat> topLearners =
                learnerAggregation.entrySet()
                        .stream()
                        .map(entry -> {

                            double[] values = entry.getValue();

                            User user =
                                    userCache.get(entry.getKey());

                            if (user == null) {
                                return null;
                            }

                            int average = values[1] > 0
                                    ? (int) Math.round(
                                    values[0] / values[1]
                            )
                                    : 0;

                            return TopLearnerStat.builder()
                                    .name(resolveUserName(user))
                                    .avg(average)
                                    .attempts((int) values[1])
                                    .build();
                        })
                        .filter(item -> item != null)
                        .sorted(
                                Comparator.comparing(
                                        TopLearnerStat::getAvg,
                                        Comparator.reverseOrder()
                                )
                        )
                        .limit(5)
                        .collect(Collectors.toList());

        return AdminReportsResponse.builder()
                .totalInterviews(completed.size())
                .totalCompanies(totalCompanies)
                .totalCategories(totalCategories)
                .byCategory(byCategory)
                .byCompany(byCompany)
                .topLearners(topLearners)
                .build();
    }

    private List<Interview> getCompletedInterviews(
            Map<String, User> existingUsers) {

        List<Interview> interviews =
                interviewRepository.findByStatus(
                        InterviewStatus.COMPLETED
                );

        if (interviews == null || interviews.isEmpty()) {
            return new ArrayList<>();
        }

        return interviews.stream()
                .filter(interview -> interview != null)
                .filter(interview ->
                        interview.getUserId() != null &&
                                !interview.getUserId().isBlank()
                )
                .filter(interview ->
                        existingUsers.containsKey(
                                interview.getUserId()
                        )
                )
                .collect(Collectors.toList());
    }

    private Map<String, Company> loadCompanies() {

        Map<String, Company> cache = new HashMap<>();

        List<Company> companies =
                companyRepository.findAll();

        if (companies == null) {
            return cache;
        }

        for (Company company : companies) {

            if (company != null &&
                    company.getId() != null &&
                    !company.getId().isBlank()) {

                cache.put(
                        company.getId(),
                        company
                );
            }
        }

        return cache;
    }

    private Map<String, Category> loadCategories() {

        Map<String, Category> cache = new HashMap<>();

        List<Category> categories =
                categoryRepository.findAll();

        if (categories == null) {
            return cache;
        }

        for (Category category : categories) {

            if (category != null &&
                    category.getId() != null &&
                    !category.getId().isBlank()) {

                cache.put(
                        category.getId(),
                        category
                );
            }
        }

        return cache;
    }

    private Map<String, User> loadUsers() {

        Map<String, User> cache = new HashMap<>();

        List<User> users =
                userRepository.findAllByRole(
                        UserRole.USER,
                        org.springframework.data.domain.Pageable.unpaged()
                ).getContent();

        if (users == null) {
            return cache;
        }

        for (User user : users) {

            if (user != null &&
                    user.getId() != null &&
                    !user.getId().isBlank()) {

                cache.put(
                        user.getId(),
                        user
                );
            }
        }

        return cache;
    }

    private RecentInterviewResponse toRecentInterviewResponse(
            Interview interview,
            Map<String, Company> companies,
            Map<String, Category> categories,
            Map<String, User> users) {

        User user = users.get(interview.getUserId());

        if (user == null) {
            return null;
        }

        return RecentInterviewResponse.builder()
                .interviewId(interview.getId())
                .userName(resolveUserName(user))
                .categoryName(
                        resolveCategoryName(
                                interview.getCategoryId(),
                                categories
                        )
                )
                .companyName(
                        resolveCompanyName(
                                interview.getCompanyId(),
                                companies
                        )
                )
                .difficulty(interview.getDifficulty())
                .percentage(
                        sanitizePercentageValue(
                                interview.getPercentage()
                        )
                )
                .completedAt(interview.getEndTime())
                .build();
    }

    private String resolveCompanyName(
            String companyId,
            Map<String, Company> companies) {

        if (companyId == null ||
                companyId.isBlank()) {

            return "Unknown";
        }

        Company company =
                companies.get(companyId);

        if (company == null ||
                company.getName() == null ||
                company.getName().isBlank()) {

            return "Unknown";
        }

        return company.getName();
    }

    private String resolveCategoryName(
            String categoryId,
            Map<String, Category> categories) {

        if (categoryId == null ||
                categoryId.isBlank()) {

            return "Unknown";
        }

        Category category =
                categories.get(categoryId);

        if (category == null ||
                category.getName() == null ||
                category.getName().isBlank()) {

            return "Unknown";
        }

        return category.getName();
    }

    private String resolveUserName(User user) {

        if (user == null) {
            return "";
        }

        if (user.getName() != null &&
                !user.getName().isBlank()) {

            return user.getName();
        }

        if (user.getEmail() != null &&
                !user.getEmail().isBlank()) {

            return user.getEmail();
        }

        return "";
    }

    private double calculateAverageScore(
            List<Interview> interviews) {

        if (interviews == null ||
                interviews.isEmpty()) {

            return 0.0;
        }

        return interviews.stream()
                .mapToDouble(
                        interview ->
                                sanitizePercentage(
                                        interview.getPercentage()
                                )
                )
                .average()
                .orElse(0.0);
    }

    private double sanitizePercentage(
            Double value) {

        if (value == null ||
                value.isNaN() ||
                value.isInfinite()) {

            return 0.0;
        }

        return Math.max(
                0.0,
                Math.min(100.0, value)
        );
    }

    private Double sanitizePercentageValue(
            Double value) {

        return sanitizePercentage(value);
    }

    private double round(double value) {

        return Math.round(
                value * 10.0
        ) / 10.0;
    }
}