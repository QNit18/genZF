package com.qnit18.main_service.controller;

import com.qnit18.main_service.dto.request.PortfolioCreationRequest;
import com.qnit18.main_service.dto.response.ApiBaseResponse;
import com.qnit18.main_service.dto.response.PortfolioResponse;
import com.qnit18.main_service.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/portfolios")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Portfolio Management", description = "APIs for managing user portfolios and calculating net worth")
public class PortfolioController {
    PortfolioService portfolioService;

    @PostMapping
    ApiBaseResponse<PortfolioResponse> createPortfolio(@RequestBody @Valid PortfolioCreationRequest request) {
        log.info("Creating portfolio: {}", request);
        ApiBaseResponse<PortfolioResponse> response = new ApiBaseResponse<>();
        response.setResult(portfolioService.createPortfolio(request));
        return response;
    }

    @GetMapping("/{id}")
    ApiBaseResponse<PortfolioResponse> getPortfolioById(@PathVariable UUID id) {
        ApiBaseResponse<PortfolioResponse> response = new ApiBaseResponse<>();
        response.setResult(portfolioService.getPortfolioById(id));
        return response;
    }

    @GetMapping("/user/{userId}")
    ApiBaseResponse<PortfolioResponse> getPortfolioByUserId(@PathVariable String userId) {
        ApiBaseResponse<PortfolioResponse> response = new ApiBaseResponse<>();
        response.setResult(portfolioService.getPortfolioByUserId(userId));
        return response;
    }

    @DeleteMapping("/{id}")
    ApiBaseResponse<String> deletePortfolio(@PathVariable UUID id) {
        portfolioService.deletePortfolio(id);
        ApiBaseResponse<String> response = new ApiBaseResponse<>();
        response.setResult("Portfolio has been deleted");
        return response;
    }
}

