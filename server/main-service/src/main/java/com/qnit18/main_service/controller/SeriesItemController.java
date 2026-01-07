package com.qnit18.main_service.controller;

import com.qnit18.main_service.dto.request.SeriesItemCreationRequest;
import com.qnit18.main_service.dto.response.ApiBaseResponse;
import com.qnit18.main_service.dto.response.SeriesItemResponse;
import com.qnit18.main_service.service.SeriesItemService;
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
@RequestMapping("/series-items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeriesItemController {
    SeriesItemService seriesItemService;

    @PostMapping
    ApiBaseResponse<SeriesItemResponse> createSeriesItem(@RequestBody @Valid SeriesItemCreationRequest request) {
        log.info("Creating series item: {}", request);
        ApiBaseResponse<SeriesItemResponse> response = new ApiBaseResponse<>();
        response.setResult(seriesItemService.createSeriesItem(request));
        return response;
    }

    @GetMapping("/{id}")
    ApiBaseResponse<SeriesItemResponse> getSeriesItemById(@PathVariable UUID id) {
        ApiBaseResponse<SeriesItemResponse> response = new ApiBaseResponse<>();
        response.setResult(seriesItemService.getSeriesItemById(id));
        return response;
    }

    @GetMapping("/chart-data/{chartDataId}")
    ApiBaseResponse<List<SeriesItemResponse>> getSeriesItemsByChartDataId(@PathVariable UUID chartDataId) {
        ApiBaseResponse<List<SeriesItemResponse>> response = new ApiBaseResponse<>();
        response.setResult(seriesItemService.getSeriesItemsByChartDataId(chartDataId));
        return response;
    }

    @DeleteMapping("/{id}")
    ApiBaseResponse<String> deleteSeriesItem(@PathVariable UUID id) {
        seriesItemService.deleteSeriesItem(id);
        ApiBaseResponse<String> response = new ApiBaseResponse<>();
        response.setResult("Series item has been deleted");
        return response;
    }
}

