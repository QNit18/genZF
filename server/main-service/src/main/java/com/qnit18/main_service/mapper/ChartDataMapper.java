package com.qnit18.main_service.mapper;

import com.qnit18.main_service.dto.request.ChartDataCreationRequest;
import com.qnit18.main_service.dto.request.ChartDataUpdateRequest;
import com.qnit18.main_service.dto.response.ChartDataResponse;
import com.qnit18.main_service.entity.ChartData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {SeriesItemMapper.class})
public interface ChartDataMapper {
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "seriesItems", ignore = true)
    ChartData toChartData(ChartDataCreationRequest request);
    
    @Mapping(target = "assetId", source = "asset.id")
    ChartDataResponse toChartDataResponse(ChartData chartData);
    
    void updateChartData(@MappingTarget ChartData chartData, ChartDataUpdateRequest request);
}

