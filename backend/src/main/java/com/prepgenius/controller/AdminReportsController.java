package com.prepgenius.controller;

import com.prepgenius.dto.AdminDashboardResponse;
import com.prepgenius.dto.AdminReportsResponse;
import com.prepgenius.service.AdminReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportsController {

    private final AdminReportsService adminReportsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(adminReportsService.getDashboardStats());
    }

    @GetMapping("/reports")
    public ResponseEntity<AdminReportsResponse> getReports() {
        return ResponseEntity.ok(adminReportsService.getReports());
    }
}
