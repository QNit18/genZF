package com.qnit18.main_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "budget_rules")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    String userId;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal monthlyIncome;

    @JsonIgnore
    @OneToMany(mappedBy = "budgetRule", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BudgetEntry> budgets;
}

