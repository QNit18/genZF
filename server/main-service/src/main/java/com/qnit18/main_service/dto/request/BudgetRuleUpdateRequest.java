package com.qnit18.main_service.dto.request;

import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetRuleUpdateRequest {
    BigDecimal monthlyIncome;
    @Valid
    List<BudgetEntryCreationRequest> budgets;
}

