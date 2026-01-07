package com.qnit18.main_service.service;

import com.qnit18.main_service.client.AuthServiceClient;
import com.qnit18.main_service.constant.AssetCategory;
import com.qnit18.main_service.dto.request.PortfolioCreationRequest;
import com.qnit18.main_service.dto.request.PortfolioTransactionRequest;
import com.qnit18.main_service.dto.response.*;
import com.qnit18.main_service.entity.Asset;
import com.qnit18.main_service.entity.AssetUser;
import com.qnit18.main_service.entity.Portfolio;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import com.qnit18.main_service.mapper.AssetUserMapper;
import com.qnit18.main_service.mapper.PortfolioMapper;
import com.qnit18.main_service.repository.AssetRepository;
import com.qnit18.main_service.repository.AssetUserRepository;
import com.qnit18.main_service.repository.PortfolioRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PortfolioService {
    PortfolioRepository portfolioRepository;
    AssetUserRepository assetUserRepository;
    AssetRepository assetRepository;
    AuthServiceClient authServiceClient;
    PortfolioMapper portfolioMapper;
    AssetUserMapper assetUserMapper;

    @Transactional
    public PortfolioResponse createPortfolio(PortfolioCreationRequest request) {
        // Validate user exists in auth-service
        if (!authServiceClient.validateUser(request.getUserId())) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        if (portfolioRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Portfolio portfolio = portfolioMapper.toPortfolio(request);
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        
        PortfolioResponse response = portfolioMapper.toPortfolioResponse(savedPortfolio);
        calculatePortfolioMetrics(response, savedPortfolio);
        return response;
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioById(UUID id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PORTFOLIO_NOT_FOUND));
        
        PortfolioResponse response = portfolioMapper.toPortfolioResponse(portfolio);
        calculatePortfolioMetrics(response, portfolio);
        return response;
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioByUserId(String userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PORTFOLIO_NOT_FOUND));
        
        PortfolioResponse response = portfolioMapper.toPortfolioResponse(portfolio);
        calculatePortfolioMetrics(response, portfolio);
        return response;
    }

    @Transactional
    public void deletePortfolio(UUID id) {
        if (!portfolioRepository.existsById(id)) {
            throw new AppException(ErrorCode.PORTFOLIO_NOT_FOUND);
        }
        portfolioRepository.deleteById(id);
    }

    private void calculatePortfolioMetrics(PortfolioResponse response, Portfolio portfolio) {
        if (portfolio.getAssetUsers() == null || portfolio.getAssetUsers().isEmpty()) {
            response.setTotalNetWorth(BigDecimal.ZERO);
            response.setProfit(BigDecimal.ZERO);
            return;
        }

        BigDecimal totalNetWorth = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (AssetUser assetUser : portfolio.getAssetUsers()) {
            if (Boolean.TRUE.equals(assetUser.getDeleted())) {
                continue;
            }

            BigDecimal costBasis = assetUser.getPriceBuy().multiply(assetUser.getQuantity());
            totalCost = totalCost.add(costBasis);

            BigDecimal currentValue = assetUser.getPriceSale() != null 
                    ? assetUser.getPriceSale().multiply(assetUser.getQuantity())
                    : assetUser.getPriceBuy().multiply(assetUser.getQuantity());
            
            totalNetWorth = totalNetWorth.add(currentValue);
        }

        response.setTotalNetWorth(totalNetWorth);
        response.setProfit(totalNetWorth.subtract(totalCost));
    }

    @Transactional(readOnly = true)
    public List<AssetUserResponse> getPortfolioAssets(String userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PORTFOLIO_NOT_FOUND));
        
        return assetUserRepository.findByPortfolioIdAndDeletedFalse(portfolio.getId()).stream()
                .map(assetUserMapper::toAssetUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AllocationResponse getPortfolioAllocation(String userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PORTFOLIO_NOT_FOUND));
        
        List<AssetUser> assetUsers = assetUserRepository.findByPortfolioIdAndDeletedFalse(portfolio.getId());
        
        if (assetUsers.isEmpty()) {
            return AllocationResponse.builder()
                    .assetAllocations(Collections.emptyList())
                    .categoryAllocations(Collections.emptyList())
                    .build();
        }

        // Get current prices for assets
        Map<String, BigDecimal> assetPrices = new HashMap<>();
        Map<String, AssetCategory> assetCategories = new HashMap<>();
        for (AssetUser assetUser : assetUsers) {
            Optional<Asset> assetOpt = assetRepository.findAll().stream()
                    .filter(a -> a.getAssetName().equalsIgnoreCase(assetUser.getAssetName()))
                    .findFirst();
            if (assetOpt.isPresent()) {
                Asset asset = assetOpt.get();
                assetPrices.put(assetUser.getAssetName(), asset.getCurrentPrice());
                assetCategories.put(assetUser.getAssetName(), asset.getCategory());
            } else {
                // Use priceSale if available, otherwise priceBuy
                BigDecimal price = assetUser.getPriceSale() != null 
                    ? assetUser.getPriceSale() 
                    : assetUser.getPriceBuy();
                assetPrices.put(assetUser.getAssetName(), price);
            }
        }

        // Calculate total portfolio value
        BigDecimal totalValue = assetUsers.stream()
                .map(au -> {
                    BigDecimal price = assetPrices.getOrDefault(au.getAssetName(), au.getPriceBuy());
                    return price.multiply(au.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return AllocationResponse.builder()
                    .assetAllocations(Collections.emptyList())
                    .categoryAllocations(Collections.emptyList())
                    .build();
        }

        // Calculate asset allocations
        List<AssetAllocation> assetAllocations = assetUsers.stream()
                .map(au -> {
                    BigDecimal price = assetPrices.getOrDefault(au.getAssetName(), au.getPriceBuy());
                    BigDecimal value = price.multiply(au.getQuantity());
                    BigDecimal percentage = value.divide(totalValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    
                    return AssetAllocation.builder()
                            .assetName(au.getAssetName())
                            .value(value)
                            .percentage(percentage)
                            .build();
                })
                .sorted((a1, a2) -> a2.getPercentage().compareTo(a1.getPercentage()))
                .collect(Collectors.toList());

        // Calculate category allocations
        Map<AssetCategory, BigDecimal> categoryValues = new HashMap<>();
        for (AssetUser au : assetUsers) {
            AssetCategory category = assetCategories.getOrDefault(au.getAssetName(), AssetCategory.COMMODITY);
            BigDecimal price = assetPrices.getOrDefault(au.getAssetName(), au.getPriceBuy());
            BigDecimal value = price.multiply(au.getQuantity());
            categoryValues.merge(category, value, BigDecimal::add);
        }

        List<CategoryAllocation> categoryAllocations = categoryValues.entrySet().stream()
                .map(entry -> {
                    BigDecimal percentage = entry.getValue().divide(totalValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    
                    return CategoryAllocation.builder()
                            .category(entry.getKey())
                            .value(entry.getValue())
                            .percentage(percentage)
                            .build();
                })
                .sorted((c1, c2) -> c2.getPercentage().compareTo(c1.getPercentage()))
                .collect(Collectors.toList());

        return AllocationResponse.builder()
                .assetAllocations(assetAllocations)
                .categoryAllocations(categoryAllocations)
                .build();
    }

    @Transactional
    public AssetUserResponse addOrUpdateTransaction(String userId, PortfolioTransactionRequest request) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PORTFOLIO_NOT_FOUND));

        Optional<AssetUser> existingAssetUserOpt = assetUserRepository
                .findByPortfolioIdAndAssetNameAndDeletedFalse(portfolio.getId(), request.getAssetName());

        AssetUser assetUser;
        if (existingAssetUserOpt.isPresent()) {
            // Update existing transaction - merge quantities
            assetUser = existingAssetUserOpt.get();
            assetUser.setQuantity(assetUser.getQuantity().add(request.getQuantity()));
            
            // Update prices if provided
            if (request.getPriceBuy() != null) {
                assetUser.setPriceBuy(request.getPriceBuy());
            }
            if (request.getPriceSale() != null) {
                assetUser.setPriceSale(request.getPriceSale());
            }
            if (request.getInterestRate() != null) {
                assetUser.setInterestRate(request.getInterestRate());
            }
        } else {
            // Create new transaction
            assetUser = AssetUser.builder()
                    .portfolio(portfolio)
                    .assetName(request.getAssetName())
                    .quantity(request.getQuantity())
                    .priceBuy(request.getPriceBuy())
                    .priceSale(request.getPriceSale())
                    .interestRate(request.getInterestRate())
                    .deleted(false)
                    .build();
        }

        AssetUser savedAssetUser = assetUserRepository.save(assetUser);
        return assetUserMapper.toAssetUserResponse(savedAssetUser);
    }

    @Transactional
    public void deleteTransaction(String userId, UUID transactionId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PORTFOLIO_NOT_FOUND));

        AssetUser assetUser = assetUserRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_USER_NOT_FOUND));

        // Verify ownership
        if (!assetUser.getPortfolio().getId().equals(portfolio.getId())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Soft delete
        assetUser.setDeleted(true);
        assetUserRepository.save(assetUser);
    }
}

