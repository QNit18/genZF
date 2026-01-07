package com.qnit18.main_service.service;

import com.qnit18.main_service.dto.request.BudgetEntryCreationRequest;
import com.qnit18.main_service.dto.request.BudgetRuleCreationRequest;
import com.qnit18.main_service.dto.request.BudgetRuleUpdateRequest;
import com.qnit18.main_service.dto.response.BudgetRuleResponse;
import com.qnit18.main_service.entity.BudgetEntry;
import com.qnit18.main_service.entity.BudgetRule;
import com.qnit18.main_service.entity.User;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import com.qnit18.main_service.mapper.BudgetEntryMapper;
import com.qnit18.main_service.mapper.BudgetRuleMapper;
import com.qnit18.main_service.repository.BudgetEntryRepository;
import com.qnit18.main_service.repository.BudgetRuleRepository;
import com.qnit18.main_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BudgetRuleService {
    BudgetRuleRepository budgetRuleRepository;
    BudgetEntryRepository budgetEntryRepository;
    UserRepository userRepository;
    BudgetRuleMapper budgetRuleMapper;
    BudgetEntryMapper budgetEntryMapper;

    @Transactional
    public BudgetRuleResponse createBudgetRule(BudgetRuleCreationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (budgetRuleRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        validateBudgetPercentages(request.getBudgets());

        BudgetRule budgetRule = budgetRuleMapper.toBudgetRule(request);
        budgetRule.setUser(user);

        BudgetRule savedBudgetRule = budgetRuleRepository.save(budgetRule);

        List<BudgetEntry> budgetEntries = request.getBudgets().stream()
                .map(budgetEntryMapper::toBudgetEntry)
                .peek(entry -> entry.setBudgetRule(savedBudgetRule))
                .toList();

        budgetEntryRepository.saveAll(budgetEntries);
        savedBudgetRule.setBudgets(budgetEntries);

        return budgetRuleMapper.toBudgetRuleResponse(savedBudgetRule);
    }

    @Transactional
    public BudgetRuleResponse updateBudgetRule(UUID id, BudgetRuleUpdateRequest request) {
        BudgetRule budgetRule = budgetRuleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_RULE_NOT_FOUND));

        if (request.getBudgets() != null) {
            validateBudgetPercentages(request.getBudgets());
            budgetEntryRepository.deleteAll(budgetRule.getBudgets());

            List<BudgetEntry> budgetEntries = request.getBudgets().stream()
                    .map(budgetEntryMapper::toBudgetEntry)
                    .peek(entry -> entry.setBudgetRule(budgetRule))
                    .toList();

            budgetEntryRepository.saveAll(budgetEntries);
            budgetRule.setBudgets(budgetEntries);
        }

        budgetRuleMapper.updateBudgetRule(budgetRule, request);
        return budgetRuleMapper.toBudgetRuleResponse(budgetRuleRepository.save(budgetRule));
    }

    @Transactional(readOnly = true)
    public BudgetRuleResponse getBudgetRuleById(UUID id) {
        BudgetRule budgetRule = budgetRuleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_RULE_NOT_FOUND));
        return budgetRuleMapper.toBudgetRuleResponse(budgetRule);
    }

    @Transactional(readOnly = true)
    public BudgetRuleResponse getBudgetRuleByUserId(UUID userId) {
        BudgetRule budgetRule = budgetRuleRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_RULE_NOT_FOUND));
        return budgetRuleMapper.toBudgetRuleResponse(budgetRule);
    }

    @Transactional
    public void deleteBudgetRule(UUID id) {
        if (!budgetRuleRepository.existsById(id)) {
            throw new AppException(ErrorCode.BUDGET_RULE_NOT_FOUND);
        }
        budgetRuleRepository.deleteById(id);
    }

    private void validateBudgetPercentages(List<BudgetEntryCreationRequest> budgets) {
        int totalPercent = budgets.stream()
                .mapToInt(BudgetEntryCreationRequest::getPercent)
                .sum();

        if (totalPercent != 100) {
            throw new AppException(ErrorCode.INVALID_BUDGET_PERCENTAGE);
        }
    }
}

