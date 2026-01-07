package com.qnit18.main_service.service;

import com.qnit18.main_service.constant.AssetCategory;
import com.qnit18.main_service.dto.request.AssetCreationRequest;
import com.qnit18.main_service.dto.request.AssetUpdateRequest;
import com.qnit18.main_service.dto.response.AssetResponse;
import com.qnit18.main_service.entity.Asset;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import com.qnit18.main_service.mapper.AssetMapper;
import com.qnit18.main_service.repository.AssetRepository;
import com.qnit18.main_service.repository.AssetSpecification;
import org.springframework.data.jpa.domain.Specification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssetService {
    AssetRepository assetRepository;
    AssetMapper assetMapper;

    @Transactional
    public AssetResponse createAsset(AssetCreationRequest request) {
        if (assetRepository.existsBySymbol(request.getSymbol())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Asset asset = assetMapper.toAsset(request);
        asset.setLastUpdated(LocalDateTime.now());
        return assetMapper.toAssetResponse(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponse updateAsset(UUID id, AssetUpdateRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_NOT_FOUND));

        assetMapper.updateAsset(asset, request);
        if (request.getCurrentPrice() != null) {
            asset.setLastUpdated(LocalDateTime.now());
        }

        return assetMapper.toAssetResponse(assetRepository.save(asset));
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetById(UUID id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_NOT_FOUND));
        return assetMapper.toAssetResponse(asset);
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetBySymbol(String symbol) {
        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_NOT_FOUND));
        return assetMapper.toAssetResponse(asset);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(assetMapper::toAssetResponse)
                .toList();
    }

    @Transactional
    public void deleteAsset(UUID id) {
        if (!assetRepository.existsById(id)) {
            throw new AppException(ErrorCode.ASSET_NOT_FOUND);
        }
        assetRepository.deleteById(id);
    }

    @Transactional
    public AssetResponse updatePrice(UUID id, AssetUpdateRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_NOT_FOUND));

        if (request.getCurrentPrice() != null) {
            BigDecimal oldPrice = asset.getCurrentPrice();
            asset.setCurrentPrice(request.getCurrentPrice());
            
            if (asset.getOpen() != null) {
                BigDecimal change = request.getCurrentPrice().subtract(asset.getOpen());
                asset.setChangeValue(change);
                asset.setChangePercentage((change.divide(asset.getOpen(), 4, RoundingMode.HALF_UP))
                        .multiply(BigDecimal.valueOf(100)).floatValue());
            }
            
            if (request.getCurrentPrice().compareTo(asset.getHigh()) > 0) {
                asset.setHigh(request.getCurrentPrice());
            }
            if (request.getCurrentPrice().compareTo(asset.getLow()) < 0) {
                asset.setLow(request.getCurrentPrice());
            }
            
            asset.setLastUpdated(LocalDateTime.now());
        }

        if (request.getVolume() != null) {
            asset.setVolume(request.getVolume());
        }

        if (request.getMarketStatus() != null) {
            asset.setMarketStatus(request.getMarketStatus());
        }

        return assetMapper.toAssetResponse(assetRepository.save(asset));
    }

    @Transactional(readOnly = true)
    public Page<AssetResponse> searchAssets(String q, AssetCategory category, Pageable pageable) {
        Specification<Asset> spec = AssetSpecification.searchAssets(q, category);
        return assetRepository.findAll(spec, pageable)
                .map(assetMapper::toAssetResponse);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getHomeAssets() {
        List<Asset> homeAssets = assetRepository.findByAssetHomeTrueOrderByChangePercentageDesc();
        // Limit to 9 assets
        return homeAssets.stream()
                .limit(9)
                .map(assetMapper::toAssetResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllAssetNames() {
        return assetRepository.findAll().stream()
                .map(Asset::getAssetName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}

