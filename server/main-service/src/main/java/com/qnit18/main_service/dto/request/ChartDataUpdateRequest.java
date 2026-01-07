package com.qnit18.main_service.dto.request;

import com.qnit18.main_service.constant.Timeframe;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChartDataUpdateRequest {
    Timeframe timeframe;
}

