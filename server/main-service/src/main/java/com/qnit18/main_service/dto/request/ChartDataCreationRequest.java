package com.qnit18.main_service.dto.request;

import com.qnit18.main_service.constant.Timeframe;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChartDataCreationRequest {
    @NotNull(message = "Asset ID is required")
    UUID assetId;

    @NotNull(message = "Timeframe is required")
    Timeframe timeframe;
}

