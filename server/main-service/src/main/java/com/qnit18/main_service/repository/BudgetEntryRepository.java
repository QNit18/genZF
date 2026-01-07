package com.qnit18.main_service.repository;

import com.qnit18.main_service.entity.BudgetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetEntryRepository extends JpaRepository<BudgetEntry, UUID> {
    List<BudgetEntry> findByBudgetRuleId(UUID budgetRuleId);
}

