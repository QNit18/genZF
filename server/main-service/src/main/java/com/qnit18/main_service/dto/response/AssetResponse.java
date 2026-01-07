package com.qnit18.main_service.dto.response;

import com.qnit18.main_service.constant.AssetCategory;
import com.qnit18.main_service.constant.MarketStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssetResponse {
    UUID id;
    String symbol;
    String assetName;
    AssetCategory category;
    BigDecimal currentPrice;
    Float changePercentage;
    BigDecimal changeValue;
    LocalDateTime lastUpdated;
    String currency;
    BigDecimal open;
    BigDecimal high;
    BigDecimal low;
    Long volume;
    MarketStatus marketStatus;
}

