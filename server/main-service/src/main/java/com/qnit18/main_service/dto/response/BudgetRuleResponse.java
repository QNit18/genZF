package com.qnit18.main_service.dto.response;

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
public class BudgetRuleResponse {
    UUID id;
    String userId;
    BigDecimal monthlyIncome;
    List<BudgetEntryResponse> budgets;
}

