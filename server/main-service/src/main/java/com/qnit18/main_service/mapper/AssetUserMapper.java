package com.qnit18.main_service.mapper;

import com.qnit18.main_service.dto.request.AssetUserCreationRequest;
import com.qnit18.main_service.dto.request.AssetUserUpdateRequest;
import com.qnit18.main_service.dto.response.AssetUserResponse;
import com.qnit18.main_service.entity.AssetUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AssetUserMapper {
    @Mapping(target = "portfolio", ignore = true)
    AssetUser toAssetUser(AssetUserCreationRequest request);
    
    @Mapping(target = "portfolioId", source = "portfolio.id")
    AssetUserResponse toAssetUserResponse(AssetUser assetUser);
    
    void updateAssetUser(@MappingTarget AssetUser assetUser, AssetUserUpdateRequest request);
}

