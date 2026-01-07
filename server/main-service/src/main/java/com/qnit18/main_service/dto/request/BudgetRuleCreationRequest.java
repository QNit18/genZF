package com.qnit18.main_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetRuleCreationRequest {
    @NotNull(message = "User ID is required")
    UUID userId;

    @NotNull(message = "Monthly income is required")
    BigDecimal monthlyIncome;

    @Valid
    @NotNull(message = "Budgets are required")
    List<BudgetEntryCreationRequest> budgets;
}

