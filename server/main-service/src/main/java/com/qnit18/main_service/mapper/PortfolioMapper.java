package com.qnit18.main_service.mapper;

import com.qnit18.main_service.dto.request.PortfolioCreationRequest;
import com.qnit18.main_service.dto.response.PortfolioResponse;
import com.qnit18.main_service.entity.Portfolio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {
    @Mapping(target = "assetUsers", ignore = true)
    @Mapping(target = "totalNetWorth", ignore = true)
    @Mapping(target = "profit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Portfolio toPortfolio(PortfolioCreationRequest request);
    
    PortfolioResponse toPortfolioResponse(Portfolio portfolio);
    
    void updatePortfolio(@MappingTarget Portfolio portfolio, PortfolioCreationRequest request);
}

