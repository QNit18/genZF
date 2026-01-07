package com.qnit18.main_service.dto.request;

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
public class PortfolioTransactionRequest {
    @NotBlank(message = "Asset name is required")
    String assetName;

    @NotNull(message = "Quantity is required")
    BigDecimal quantity;

    @NotNull(message = "Buy price is required")
    BigDecimal priceBuy;

    BigDecimal priceSale;
    BigDecimal interestRate;
}

