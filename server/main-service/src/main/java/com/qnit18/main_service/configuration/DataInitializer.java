package com.qnit18.main_service.configuration;

import com.qnit18.main_service.constant.AssetCategory;
import com.qnit18.main_service.constant.MarketStatus;
import com.qnit18.main_service.entity.Asset;
import com.qnit18.main_service.entity.AssetUser;
import com.qnit18.main_service.entity.Portfolio;
import com.qnit18.main_service.repository.AssetRepository;
import com.qnit18.main_service.repository.AssetUserRepository;
import com.qnit18.main_service.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AssetRepository assetRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetUserRepository assetUserRepository;

    // Example user IDs - these should exist in auth-service
    private static final String USER_ID_1 = "550e8400-e29b-41d4-a716-446655440000";
    private static final String USER_ID_2 = "550e8400-e29b-41d4-a716-446655440001";
    private static final String USER_ID_3 = "550e8400-e29b-41d4-a716-446655440002";

    @Override
    @Transactional
    public void run(String... args) {
        if (portfolioRepository.count() > 0) {
            log.info("Data already exists, skipping initialization");
            return;
        }

        log.info("Initializing example data...");
        log.warn("Note: User IDs are hardcoded. Ensure these users exist in auth-service:");
        log.warn("  User 1: {}", USER_ID_1);
        log.warn("  User 2: {}", USER_ID_2);
        log.warn("  User 3: {}", USER_ID_3);

        // Create Assets
        Asset gold = createAsset("XAU/USD", "Gold", AssetCategory.COMMODITY, 
                new BigDecimal("2650.50"), new BigDecimal("2655.00"), new BigDecimal("2645.00"), 
                new BigDecimal("2652.75"), 0.09f, new BigDecimal("2.25"), 1250000L, MarketStatus.OPEN);
        
        Asset bitcoin = createAsset("BTC/USD", "Bitcoin", AssetCategory.CRYPTO,
                new BigDecimal("67500.00"), new BigDecimal("68000.00"), new BigDecimal("67000.00"),
                new BigDecimal("67550.00"), 0.07f, new BigDecimal("50.00"), 2500000L, MarketStatus.OPEN);
        
        Asset eurusd = createAsset("EUR/USD", "Euro/US Dollar", AssetCategory.FOREX,
                new BigDecimal("1.0850"), new BigDecimal("1.0860"), new BigDecimal("1.0840"),
                new BigDecimal("1.0855"), 0.05f, new BigDecimal("0.0005"), 5000000L, MarketStatus.OPEN);
        
        Asset silver = createAsset("XAG/USD", "Silver", AssetCategory.COMMODITY,
                new BigDecimal("32.15"), new BigDecimal("32.30"), new BigDecimal("32.00"),
                new BigDecimal("32.20"), 0.16f, new BigDecimal("0.05"), 850000L, MarketStatus.OPEN);
        
        Asset ethereum = createAsset("ETH/USD", "Ethereum", AssetCategory.CRYPTO,
                new BigDecimal("3450.00"), new BigDecimal("3480.00"), new BigDecimal("3420.00"),
                new BigDecimal("3455.00"), 0.14f, new BigDecimal("5.00"), 1800000L, MarketStatus.OPEN);

        // Create Portfolios (using userId strings)
        Portfolio portfolio1 = createPortfolio(USER_ID_1);
        Portfolio portfolio2 = createPortfolio(USER_ID_2);
        Portfolio portfolio3 = createPortfolio(USER_ID_3);

        // Create Asset Users (Holdings)
        createAssetUser(portfolio1, "Gold", new BigDecimal("10.5"), new BigDecimal("2600.00"), 
                new BigDecimal("2650.50"), new BigDecimal("2.5"), false);
        createAssetUser(portfolio1, "Bitcoin", new BigDecimal("0.5"), new BigDecimal("65000.00"),
                new BigDecimal("67500.00"), new BigDecimal("3.0"), false);
        
        createAssetUser(portfolio2, "Ethereum", new BigDecimal("5.0"), new BigDecimal("3400.00"),
                new BigDecimal("3450.00"), new BigDecimal("2.8"), false);
        createAssetUser(portfolio2, "Silver", new BigDecimal("100.0"), new BigDecimal("31.00"),
                new BigDecimal("32.15"), new BigDecimal("1.5"), false);
        
        createAssetUser(portfolio3, "Gold", new BigDecimal("25.0"), new BigDecimal("2550.00"),
                new BigDecimal("2650.50"), new BigDecimal("2.0"), false);
        createAssetUser(portfolio3, "EUR/USD", new BigDecimal("50000.0"), new BigDecimal("1.0800"),
                new BigDecimal("1.0850"), new BigDecimal("1.0"), false);
        createAssetUser(portfolio3, "Bitcoin", new BigDecimal("1.0"), new BigDecimal("70000.00"),
                null, new BigDecimal("4.0"), true); // Deleted holding

        log.info("Example data initialization completed successfully!");
        log.info("Created: 5 assets, 3 portfolios, 7 asset holdings");
    }

    private Asset createAsset(String symbol, String assetName, AssetCategory category,
                              BigDecimal open, BigDecimal high, BigDecimal low,
                              BigDecimal currentPrice, Float changePercentage, BigDecimal changeValue,
                              Long volume, MarketStatus marketStatus) {
        Asset asset = Asset.builder()
                .symbol(symbol)
                .assetName(assetName)
                .category(category)
                .open(open)
                .high(high)
                .low(low)
                .currentPrice(currentPrice)
                .changePercentage(changePercentage)
                .changeValue(changeValue)
                .volume(volume)
                .marketStatus(marketStatus)
                .currency("USD")
                .lastUpdated(LocalDateTime.now())
                .build();
        return assetRepository.save(asset);
    }

    private Portfolio createPortfolio(String userId) {
        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .build();
        return portfolioRepository.save(portfolio);
    }

    private AssetUser createAssetUser(Portfolio portfolio, String assetName, BigDecimal quantity,
                                      BigDecimal priceBuy, BigDecimal priceSale, BigDecimal interestRate,
                                      Boolean deleted) {
        AssetUser assetUser = AssetUser.builder()
                .portfolio(portfolio)
                .assetName(assetName)
                .quantity(quantity)
                .priceBuy(priceBuy)
                .priceSale(priceSale)
                .interestRate(interestRate)
                .deleted(deleted != null ? deleted : false)
                .build();
        return assetUserRepository.save(assetUser);
    }
}

