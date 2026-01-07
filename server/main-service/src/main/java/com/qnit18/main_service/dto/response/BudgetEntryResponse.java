package com.qnit18.main_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetEntryResponse {
    UUID id;
    UUID budgetRuleId;
    String name;
    Integer percent;
}

