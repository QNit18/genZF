package com.qnit18.main_service.service;

import com.qnit18.main_service.dto.request.BudgetEntryCreationRequest;
import com.qnit18.main_service.dto.response.BudgetEntryResponse;
import com.qnit18.main_service.entity.BudgetEntry;
import com.qnit18.main_service.entity.BudgetRule;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import com.qnit18.main_service.mapper.BudgetEntryMapper;
import com.qnit18.main_service.repository.BudgetEntryRepository;
import com.qnit18.main_service.repository.BudgetRuleRepository;
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
public class BudgetEntryService {
    BudgetEntryRepository budgetEntryRepository;
    BudgetRuleRepository budgetRuleRepository;
    BudgetEntryMapper budgetEntryMapper;

    @Transactional
    public BudgetEntryResponse createBudgetEntry(BudgetEntryCreationRequest request, UUID budgetRuleId) {
        BudgetRule budgetRule = budgetRuleRepository.findById(budgetRuleId)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_RULE_NOT_FOUND));

        BudgetEntry budgetEntry = budgetEntryMapper.toBudgetEntry(request);
        budgetEntry.setBudgetRule(budgetRule);
        return budgetEntryMapper.toBudgetEntryResponse(budgetEntryRepository.save(budgetEntry));
    }

    @Transactional(readOnly = true)
    public BudgetEntryResponse getBudgetEntryById(UUID id) {
        BudgetEntry budgetEntry = budgetEntryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_ENTRY_NOT_FOUND));
        return budgetEntryMapper.toBudgetEntryResponse(budgetEntry);
    }

    @Transactional(readOnly = true)
    public List<BudgetEntryResponse> getBudgetEntriesByBudgetRuleId(UUID budgetRuleId) {
        return budgetEntryRepository.findByBudgetRuleId(budgetRuleId).stream()
                .map(budgetEntryMapper::toBudgetEntryResponse)
                .toList();
    }

    @Transactional
    public void deleteBudgetEntry(UUID id) {
        if (!budgetEntryRepository.existsById(id)) {
            throw new AppException(ErrorCode.BUDGET_ENTRY_NOT_FOUND);
        }
        budgetEntryRepository.deleteById(id);
    }
}

