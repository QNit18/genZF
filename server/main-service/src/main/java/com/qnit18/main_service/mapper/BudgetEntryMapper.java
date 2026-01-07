package com.qnit18.main_service.mapper;

import com.qnit18.main_service.dto.request.BudgetEntryCreationRequest;
import com.qnit18.main_service.dto.response.BudgetEntryResponse;
import com.qnit18.main_service.entity.BudgetEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BudgetEntryMapper {
    @Mapping(target = "budgetRule", ignore = true)
    BudgetEntry toBudgetEntry(BudgetEntryCreationRequest request);
    
    @Mapping(target = "budgetRuleId", source = "budgetRule.id")
    BudgetEntryResponse toBudgetEntryResponse(BudgetEntry budgetEntry);
    
    void updateBudgetEntry(@MappingTarget BudgetEntry budgetEntry, BudgetEntryCreationRequest request);
}

