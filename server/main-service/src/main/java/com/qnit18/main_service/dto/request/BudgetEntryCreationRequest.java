package com.qnit18.main_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetEntryCreationRequest {
    @NotBlank(message = "Budget name is required")
    String name;

    @NotNull(message = "Budget percent is required")
    Integer percent;
}

