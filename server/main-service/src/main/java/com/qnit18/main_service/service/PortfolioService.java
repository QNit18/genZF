package com.qnit18.main_service.service;

import com.qnit18.main_service.dto.request.PortfolioCreationRequest;
import com.qnit18.main_service.dto.response.PortfolioResponse;
import com.qnit18.main_service.entity.AssetUser;
import com.qnit18.main_service.entity.Portfolio;
import com.qnit18.main_service.entity.User;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import com.qnit18.main_service.mapper.PortfolioMapper;
import com.qnit18.main_service.repository.PortfolioRepository;
import com.qnit18.main_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PortfolioService {
    PortfolioRepository portfolioRepository;
    UserRepository userRepository;
    PortfolioMapper portfolioMapper;

    @Transactional
    public PortfolioResponse createPortfolio(PortfolioCreationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (portfolioRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Portfolio portfolio = portfolioMapper.toPortfolio(request);
        portfolio.setUser(user);
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
    public PortfolioResponse getPortfolioByUserId(UUID userId) {
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
}

