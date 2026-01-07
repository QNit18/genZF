package com.qnit18.main_service.mapper;

import com.qnit18.main_service.dto.request.AssetCreationRequest;
import com.qnit18.main_service.dto.request.AssetUpdateRequest;
import com.qnit18.main_service.dto.response.AssetResponse;
import com.qnit18.main_service.entity.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    Asset toAsset(AssetCreationRequest request);
    AssetResponse toAssetResponse(Asset asset);
    void updateAsset(@MappingTarget Asset asset, AssetUpdateRequest request);
}

