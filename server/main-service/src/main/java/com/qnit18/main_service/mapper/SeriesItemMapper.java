package com.qnit18.main_service.mapper;

import com.qnit18.main_service.dto.request.SeriesItemCreationRequest;
import com.qnit18.main_service.dto.response.SeriesItemResponse;
import com.qnit18.main_service.entity.SeriesItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeriesItemMapper {
    @Mapping(target = "chartData", ignore = true)
    SeriesItem toSeriesItem(SeriesItemCreationRequest request);
    
    @Mapping(target = "chartDataId", source = "chartData.id")
    SeriesItemResponse toSeriesItemResponse(SeriesItem seriesItem);
}

