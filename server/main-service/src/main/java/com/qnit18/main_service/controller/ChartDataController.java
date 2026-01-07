package com.qnit18.main_service.controller;

import com.qnit18.main_service.constant.Timeframe;
import com.qnit18.main_service.dto.request.ChartDataCreationRequest;
import com.qnit18.main_service.dto.request.ChartDataUpdateRequest;
import com.qnit18.main_service.dto.response.ApiBaseResponse;
import com.qnit18.main_service.dto.response.ChartDataResponse;
import com.qnit18.main_service.service.ChartDataService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/chart-data")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChartDataController {
    ChartDataService chartDataService;

    @PostMapping
    ApiBaseResponse<ChartDataResponse> createChartData(@RequestBody @Valid ChartDataCreationRequest request) {
        log.info("Creating chart data: {}", request);
        ApiBaseResponse<ChartDataResponse> response = new ApiBaseResponse<>();
        response.setResult(chartDataService.createChartData(request));
        return response;
    }

    @PutMapping("/{id}")
    ApiBaseResponse<ChartDataResponse> updateChartData(@PathVariable UUID id, @RequestBody @Valid ChartDataUpdateRequest request) {
        log.info("Updating chart data: {}", id);
        ApiBaseResponse<ChartDataResponse> response = new ApiBaseResponse<>();
        response.setResult(chartDataService.updateChartData(id, request));
        return response;
    }

    @GetMapping("/{id}")
    ApiBaseResponse<ChartDataResponse> getChartDataById(@PathVariable UUID id) {
        ApiBaseResponse<ChartDataResponse> response = new ApiBaseResponse<>();
        response.setResult(chartDataService.getChartDataById(id));
        return response;
    }

    @GetMapping("/asset/{assetId}")
    ApiBaseResponse<List<ChartDataResponse>> getChartDataByAssetId(@PathVariable UUID assetId) {
        ApiBaseResponse<List<ChartDataResponse>> response = new ApiBaseResponse<>();
        response.setResult(chartDataService.getChartDataByAssetId(assetId));
        return response;
    }

    @GetMapping("/asset/{assetId}/timeframe/{timeframe}")
    ApiBaseResponse<ChartDataResponse> getChartDataByAssetIdAndTimeframe(
            @PathVariable UUID assetId,
            @PathVariable Timeframe timeframe) {
        ApiBaseResponse<ChartDataResponse> response = new ApiBaseResponse<>();
        response.setResult(chartDataService.getChartDataByAssetIdAndTimeframe(assetId, timeframe));
        return response;
    }

    @DeleteMapping("/{id}")
    ApiBaseResponse<String> deleteChartData(@PathVariable UUID id) {
        chartDataService.deleteChartData(id);
        ApiBaseResponse<String> response = new ApiBaseResponse<>();
        response.setResult("Chart data has been deleted");
        return response;
    }
}

