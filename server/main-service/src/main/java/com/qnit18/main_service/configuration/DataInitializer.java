package com.qnit18.main_service.configuration;

import com.qnit18.main_service.constant.AssetCategory;
import com.qnit18.main_service.constant.MarketStatus;
import com.qnit18.main_service.constant.Timeframe;
import com.qnit18.main_service.entity.*;
import com.qnit18.main_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AssetRepository assetRepository;
    private final PortfolioRepository portfolioRepository;
    private final BudgetRuleRepository budgetRuleRepository;
    private final BudgetEntryRepository budgetEntryRepository;
    private final AssetUserRepository assetUserRepository;
    private final ChartDataRepository chartDataRepository;
    private final SeriesItemRepository seriesItemRepository;

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

        // Create Chart Data for Gold
        ChartData goldChart1D = createChartData(gold, Timeframe.ONE_DAY);
        ChartData goldChart1W = createChartData(gold, Timeframe.ONE_WEEK);
        createSeriesItems(goldChart1D, new BigDecimal("2650.00"), 10);
        createSeriesItems(goldChart1W, new BigDecimal("2640.00"), 7);

        // Create Chart Data for Bitcoin
        ChartData btcChart1D = createChartData(bitcoin, Timeframe.ONE_DAY);
        ChartData btcChart1M = createChartData(bitcoin, Timeframe.ONE_MONTH);
        createSeriesItems(btcChart1D, new BigDecimal("67500.00"), 10);
        createSeriesItems(btcChart1M, new BigDecimal("67000.00"), 30);

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

        // Create Budget Rules (using userId strings)
        BudgetRule budgetRule1 = createBudgetRule(USER_ID_1, new BigDecimal("5000.00"));
        createBudgetEntry(budgetRule1, "Needs", 50);
        createBudgetEntry(budgetRule1, "Wants", 30);
        createBudgetEntry(budgetRule1, "Savings", 20);

        BudgetRule budgetRule2 = createBudgetRule(USER_ID_2, new BigDecimal("7500.00"));
        createBudgetEntry(budgetRule2, "Needs", 40);
        createBudgetEntry(budgetRule2, "Wants", 30);
        createBudgetEntry(budgetRule2, "Savings", 30);

        BudgetRule budgetRule3 = createBudgetRule(USER_ID_3, new BigDecimal("10000.00"));
        createBudgetEntry(budgetRule3, "Needs", 50);
        createBudgetEntry(budgetRule3, "Wants", 25);
        createBudgetEntry(budgetRule3, "Savings", 25);

        log.info("Example data initialization completed successfully!");
        log.info("Created: 5 assets, 4 chart data entries, 3 portfolios, 7 asset holdings, 3 budget rules");
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

    private ChartData createChartData(Asset asset, Timeframe timeframe) {
        ChartData chartData = ChartData.builder()
                .asset(asset)
                .timeframe(timeframe)
                .build();
        return chartDataRepository.save(chartData);
    }

    private void createSeriesItems(ChartData chartData, BigDecimal basePrice, int count) {
        List<SeriesItem> items = new ArrayList<>();
        long baseTimestamp = System.currentTimeMillis() / 1000 - (count * 3600); // Start from count hours ago
        
        for (int i = 0; i < count; i++) {
            BigDecimal price = basePrice.add(new BigDecimal(Math.random() * 20 - 10)); // Random variation
            SeriesItem item = SeriesItem.builder()
                    .chartData(chartData)
                    .timestamp(baseTimestamp + (i * 3600))
                    .price(price)
                    .build();
            items.add(item);
        }
        seriesItemRepository.saveAll(items);
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

    private BudgetRule createBudgetRule(String userId, BigDecimal monthlyIncome) {
        BudgetRule budgetRule = BudgetRule.builder()
                .userId(userId)
                .monthlyIncome(monthlyIncome)
                .build();
        return budgetRuleRepository.save(budgetRule);
    }

    private BudgetEntry createBudgetEntry(BudgetRule budgetRule, String name, Integer percent) {
        BudgetEntry entry = BudgetEntry.builder()
                .budgetRule(budgetRule)
                .name(name)
                .percent(percent)
                .build();
        return budgetEntryRepository.save(entry);
    }
}

