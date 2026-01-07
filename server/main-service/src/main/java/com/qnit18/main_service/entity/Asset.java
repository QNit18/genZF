package com.qnit18.main_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qnit18.main_service.constant.AssetCategory;
import com.qnit18.main_service.constant.MarketStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "assets")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, unique = true)
    String symbol;

    @Column(nullable = false)
    String assetName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AssetCategory category;

    @Column(nullable = false, precision = 19, scale = 4)
    BigDecimal currentPrice;

    @Column(nullable = false)
    Float changePercentage;

    @Column(nullable = false, precision = 19, scale = 4)
    BigDecimal changeValue;

    @Column(nullable = false)
    LocalDateTime lastUpdated;

    @Column(nullable = false, length = 3)
    String currency;

    @Column(nullable = false, precision = 19, scale = 4)
    BigDecimal open;

    @Column(nullable = false, precision = 19, scale = 4)
    BigDecimal high;

    @Column(nullable = false, precision = 19, scale = 4)
    BigDecimal low;

    @Column(nullable = false)
    Long volume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MarketStatus marketStatus;

    @JsonIgnore
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ChartData> chartDataList;
}
