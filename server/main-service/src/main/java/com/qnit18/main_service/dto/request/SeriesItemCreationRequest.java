package com.qnit18.main_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeriesItemCreationRequest {
    @NotNull(message = "Chart data ID is required")
    UUID chartDataId;

    @NotNull(message = "Timestamp is required")
    Long timestamp;

    @NotNull(message = "Price is required")
    BigDecimal price;
}

