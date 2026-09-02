package com.prepgenius.service;

import com.prepgenius.dto.CompanyRequest;
import com.prepgenius.dto.CompanyResponse;
import com.prepgenius.model.Company;
import com.prepgenius.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<CompanyResponse> getActiveCompanies() {
        return companyRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CompanyResponse createCompany(CompanyRequest request) {
        if (companyRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Company already exists");
        }

        Company company = Company.builder()
                .name(request.getName())
                .focus(request.getFocus())
                .logoUrl(request.getLogoUrl())
                .active(request.isActive())
                .build();

        company = companyRepository.save(company);
        return mapToResponse(company);
    }

    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable).map(this::mapToResponse);
    }

    public CompanyResponse getCompanyById(String id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return mapToResponse(company);
    }

    public CompanyResponse updateCompany(String id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getName().equalsIgnoreCase(request.getName()) &&
            companyRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Another company with this name already exists");
        }

        company.setName(request.getName());
        company.setFocus(request.getFocus());
        company.setLogoUrl(request.getLogoUrl());
        company.setActive(request.isActive());

        company = companyRepository.save(company);
        return mapToResponse(company);
    }

    public void deleteCompany(String id) {
        if (!companyRepository.existsById(id)) {
            throw new RuntimeException("Company not found");
        }
        companyRepository.deleteById(id);
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .focus(company.getFocus())
                .logoUrl(company.getLogoUrl())
                .active(company.isActive())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
