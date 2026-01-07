package com.qnit18.main_service.repository;

import com.qnit18.main_service.entity.BudgetRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRuleRepository extends JpaRepository<BudgetRule, UUID> {
    Optional<BudgetRule> findByUserId(String userId);
}

