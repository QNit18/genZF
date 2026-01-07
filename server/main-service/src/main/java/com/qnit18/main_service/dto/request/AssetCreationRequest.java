package com.qnit18.main_service.dto.request;

import com.qnit18.main_service.constant.AssetCategory;
import com.qnit18.main_service.constant.MarketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssetCreationRequest {
    @NotBlank(message = "Symbol is required")
    String symbol;

    @NotBlank(message = "Asset name is required")
    String assetName;

    @NotNull(message = "Category is required")
    AssetCategory category;

    @NotNull(message = "Current price is required")
    BigDecimal currentPrice;

    @NotNull(message = "Change percentage is required")
    Float changePercentage;

    @NotNull(message = "Change value is required")
    BigDecimal changeValue;

    @NotBlank(message = "Currency is required")
    String currency;

    @NotNull(message = "Open price is required")
    BigDecimal open;

    @NotNull(message = "High price is required")
    BigDecimal high;

    @NotNull(message = "Low price is required")
    BigDecimal low;

    @NotNull(message = "Volume is required")
    Long volume;

    @NotNull(message = "Market status is required")
    MarketStatus marketStatus;
}

