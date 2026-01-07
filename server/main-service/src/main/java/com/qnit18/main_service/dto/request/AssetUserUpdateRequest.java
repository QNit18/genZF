package com.qnit18.main_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssetUserUpdateRequest {
    BigDecimal quantity;
    BigDecimal priceBuy;
    BigDecimal priceSale;
    BigDecimal interestRate;
    Boolean deleted;
}

