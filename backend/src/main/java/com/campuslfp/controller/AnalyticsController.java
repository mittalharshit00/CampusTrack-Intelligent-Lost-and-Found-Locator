package com.campuslfp.controller;

import com.campuslfp.dto.response.AnalyticsSummary;
import com.campuslfp.dto.response.UserAnalyticsSummary;
import com.campuslfp.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public AnalyticsSummary summary() {
        return analyticsService.summary();
    }

    @GetMapping("/user")
    public UserAnalyticsSummary userSummary(Authentication authentication) {
        return analyticsService.userSummary(authentication);
    }
}
