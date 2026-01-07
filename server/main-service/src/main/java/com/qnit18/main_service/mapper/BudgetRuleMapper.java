package com.qnit18.main_service.mapper;

import com.qnit18.main_service.dto.request.BudgetRuleCreationRequest;
import com.qnit18.main_service.dto.request.BudgetRuleUpdateRequest;
import com.qnit18.main_service.dto.response.BudgetRuleResponse;
import com.qnit18.main_service.entity.BudgetRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {BudgetEntryMapper.class})
public interface BudgetRuleMapper {
    @Mapping(target = "budgets", ignore = true)
    BudgetRule toBudgetRule(BudgetRuleCreationRequest request);
    
    BudgetRuleResponse toBudgetRuleResponse(BudgetRule budgetRule);
    
    void updateBudgetRule(@MappingTarget BudgetRule budgetRule, BudgetRuleUpdateRequest request);
}

