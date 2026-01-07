package com.qnit18.main_service.dto.response;

import com.qnit18.main_service.constant.AssetCategory;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryAllocation {
    AssetCategory category;
    BigDecimal percentage;
    BigDecimal value;
}

