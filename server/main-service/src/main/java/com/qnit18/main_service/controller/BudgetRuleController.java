package com.qnit18.main_service.controller;

import com.qnit18.main_service.dto.request.BudgetRuleCreationRequest;
import com.qnit18.main_service.dto.request.BudgetRuleUpdateRequest;
import com.qnit18.main_service.dto.response.ApiBaseResponse;
import com.qnit18.main_service.dto.response.BudgetRuleResponse;
import com.qnit18.main_service.service.BudgetRuleService;
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
@RequestMapping("/budget-rules")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Budget Rule Management", description = "APIs for managing user budget rules and allocations")
public class BudgetRuleController {
    BudgetRuleService budgetRuleService;

    @PostMapping
    ApiBaseResponse<BudgetRuleResponse> createBudgetRule(@RequestBody @Valid BudgetRuleCreationRequest request) {
        log.info("Creating budget rule: {}", request);
        ApiBaseResponse<BudgetRuleResponse> response = new ApiBaseResponse<>();
        response.setResult(budgetRuleService.createBudgetRule(request));
        return response;
    }

    @PutMapping("/{id}")
    ApiBaseResponse<BudgetRuleResponse> updateBudgetRule(@PathVariable UUID id, @RequestBody @Valid BudgetRuleUpdateRequest request) {
        log.info("Updating budget rule: {}", id);
        ApiBaseResponse<BudgetRuleResponse> response = new ApiBaseResponse<>();
        response.setResult(budgetRuleService.updateBudgetRule(id, request));
        return response;
    }

    @GetMapping("/{id}")
    ApiBaseResponse<BudgetRuleResponse> getBudgetRuleById(@PathVariable UUID id) {
        ApiBaseResponse<BudgetRuleResponse> response = new ApiBaseResponse<>();
        response.setResult(budgetRuleService.getBudgetRuleById(id));
        return response;
    }

    @GetMapping("/user/{userId}")
    ApiBaseResponse<BudgetRuleResponse> getBudgetRuleByUserId(@PathVariable UUID userId) {
        ApiBaseResponse<BudgetRuleResponse> response = new ApiBaseResponse<>();
        response.setResult(budgetRuleService.getBudgetRuleByUserId(userId));
        return response;
    }

    @DeleteMapping("/{id}")
    ApiBaseResponse<String> deleteBudgetRule(@PathVariable UUID id) {
        budgetRuleService.deleteBudgetRule(id);
        ApiBaseResponse<String> response = new ApiBaseResponse<>();
        response.setResult("Budget rule has been deleted");
        return response;
    }
}

