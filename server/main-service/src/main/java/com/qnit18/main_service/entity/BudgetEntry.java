package com.qnit18.main_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "budget_entries")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_rule_id", nullable = false)
    BudgetRule budgetRule;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    Integer percent;
}

