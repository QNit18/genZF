package com.qnit18.main_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeriesItemResponse {
    UUID id;
    UUID chartDataId;
    Long timestamp;
    BigDecimal price;
}

