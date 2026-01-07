package com.qnit18.main_service.service;

import com.qnit18.main_service.dto.request.SeriesItemCreationRequest;
import com.qnit18.main_service.dto.response.SeriesItemResponse;
import com.qnit18.main_service.entity.ChartData;
import com.qnit18.main_service.entity.SeriesItem;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import com.qnit18.main_service.mapper.SeriesItemMapper;
import com.qnit18.main_service.repository.ChartDataRepository;
import com.qnit18.main_service.repository.SeriesItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeriesItemService {
    SeriesItemRepository seriesItemRepository;
    ChartDataRepository chartDataRepository;
    SeriesItemMapper seriesItemMapper;

    @Transactional
    public SeriesItemResponse createSeriesItem(SeriesItemCreationRequest request) {
        ChartData chartData = chartDataRepository.findById(request.getChartDataId())
                .orElseThrow(() -> new AppException(ErrorCode.CHART_DATA_NOT_FOUND));

        SeriesItem seriesItem = seriesItemMapper.toSeriesItem(request);
        seriesItem.setChartData(chartData);
        return seriesItemMapper.toSeriesItemResponse(seriesItemRepository.save(seriesItem));
    }

    @Transactional(readOnly = true)
    public SeriesItemResponse getSeriesItemById(UUID id) {
        SeriesItem seriesItem = seriesItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERIES_ITEM_NOT_FOUND));
        return seriesItemMapper.toSeriesItemResponse(seriesItem);
    }

    @Transactional(readOnly = true)
    public List<SeriesItemResponse> getSeriesItemsByChartDataId(UUID chartDataId) {
        return seriesItemRepository.findByChartDataId(chartDataId).stream()
                .map(seriesItemMapper::toSeriesItemResponse)
                .toList();
    }

    @Transactional
    public void deleteSeriesItem(UUID id) {
        if (!seriesItemRepository.existsById(id)) {
            throw new AppException(ErrorCode.SERIES_ITEM_NOT_FOUND);
        }
        seriesItemRepository.deleteById(id);
    }
}

