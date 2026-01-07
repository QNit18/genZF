package com.qnit18.main_service.dto.response;

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
public class AssetUserResponse {
    UUID id;
    String assetId;
    BigDecimal quantity;
    BigDecimal averagePrice;
    BigDecimal currentPrice;
    BigDecimal totalValue;
    BigDecimal profitLoss;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
