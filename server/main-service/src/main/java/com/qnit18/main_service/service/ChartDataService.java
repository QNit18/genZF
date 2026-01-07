package com.qnit18.main_service.service;

import com.qnit18.main_service.constant.Timeframe;
import com.qnit18.main_service.dto.request.ChartDataCreationRequest;
import com.qnit18.main_service.dto.request.ChartDataUpdateRequest;
import com.qnit18.main_service.dto.response.ChartDataResponse;
import com.qnit18.main_service.entity.Asset;
import com.qnit18.main_service.entity.ChartData;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import com.qnit18.main_service.mapper.ChartDataMapper;
import com.qnit18.main_service.repository.AssetRepository;
import com.qnit18.main_service.repository.ChartDataRepository;
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
public class ChartDataService {
    ChartDataRepository chartDataRepository;
    AssetRepository assetRepository;
    ChartDataMapper chartDataMapper;

    @Transactional
    public ChartDataResponse createChartData(ChartDataCreationRequest request) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_NOT_FOUND));

        ChartData chartData = chartDataMapper.toChartData(request);
        chartData.setAsset(asset);
        return chartDataMapper.toChartDataResponse(chartDataRepository.save(chartData));
    }

    @Transactional
    public ChartDataResponse updateChartData(UUID id, ChartDataUpdateRequest request) {
        ChartData chartData = chartDataRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHART_DATA_NOT_FOUND));

        chartDataMapper.updateChartData(chartData, request);
        return chartDataMapper.toChartDataResponse(chartDataRepository.save(chartData));
    }

    @Transactional(readOnly = true)
    public ChartDataResponse getChartDataById(UUID id) {
        ChartData chartData = chartDataRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHART_DATA_NOT_FOUND));
        return chartDataMapper.toChartDataResponse(chartData);
    }

    @Transactional(readOnly = true)
    public List<ChartDataResponse> getChartDataByAssetId(UUID assetId) {
        return chartDataRepository.findByAssetId(assetId).stream()
                .map(chartDataMapper::toChartDataResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChartDataResponse getChartDataByAssetIdAndTimeframe(UUID assetId, Timeframe timeframe) {
        ChartData chartData = chartDataRepository.findByAssetIdAndTimeframe(assetId, timeframe)
                .orElseThrow(() -> new AppException(ErrorCode.CHART_DATA_NOT_FOUND));
        return chartDataMapper.toChartDataResponse(chartData);
    }

    @Transactional
    public void deleteChartData(UUID id) {
        if (!chartDataRepository.existsById(id)) {
            throw new AppException(ErrorCode.CHART_DATA_NOT_FOUND);
        }
        chartDataRepository.deleteById(id);
    }
}

