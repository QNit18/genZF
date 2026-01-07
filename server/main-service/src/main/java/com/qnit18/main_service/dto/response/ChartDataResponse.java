package com.qnit18.main_service.dto.response;

import com.qnit18.main_service.constant.Timeframe;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChartDataResponse {
    UUID id;
    UUID assetId;
    Timeframe timeframe;
    List<SeriesItemResponse> seriesItems;
}

