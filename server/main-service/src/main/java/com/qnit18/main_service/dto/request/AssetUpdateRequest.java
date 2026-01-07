package com.qnit18.main_service.dto.request;

import com.qnit18.main_service.constant.MarketStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssetUpdateRequest {
    BigDecimal currentPrice;
    Float changePercentage;
    BigDecimal changeValue;
    BigDecimal open;
    BigDecimal high;
    BigDecimal low;
    Long volume;
    MarketStatus marketStatus;
}

