package com.qnit18.main_service.mapper;

import com.qnit18.main_service.dto.request.PortfolioCreationRequest;
import com.qnit18.main_service.dto.response.PortfolioResponse;
import com.qnit18.main_service.entity.Portfolio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {AssetUserMapper.class})
public interface PortfolioMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "assetUsers", ignore = true)
    @Mapping(target = "totalNetWorth", ignore = true)
    @Mapping(target = "profit", ignore = true)
    Portfolio toPortfolio(PortfolioCreationRequest request);
    
    @Mapping(target = "userId", source = "user.id")
    PortfolioResponse toPortfolioResponse(Portfolio portfolio);
    
    void updatePortfolio(@MappingTarget Portfolio portfolio, PortfolioCreationRequest request);
}

