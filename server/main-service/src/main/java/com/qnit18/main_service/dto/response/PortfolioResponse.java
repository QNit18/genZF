package com.qnit18.main_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PortfolioResponse {
    UUID id;
    UUID userId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    BigDecimal totalNetWorth;
    BigDecimal profit;
    List<AssetUserResponse> assetUsers;
}

