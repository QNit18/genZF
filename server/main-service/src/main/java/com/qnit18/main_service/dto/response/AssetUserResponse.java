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
    UUID portfolioId;
    String assetName;
    BigDecimal quantity;
    BigDecimal priceBuy;
    BigDecimal priceSale;
    BigDecimal interestRate;
    Boolean deleted;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

