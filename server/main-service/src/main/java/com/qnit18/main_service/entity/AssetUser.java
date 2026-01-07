package com.qnit18.main_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "asset_users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssetUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    Portfolio portfolio;

    @Column(nullable = false)
    String assetName;

    @Column(nullable = false, precision = 19, scale = 4)
    BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    BigDecimal priceBuy;

    @Column(precision = 19, scale = 4)
    BigDecimal priceSale;

    @Column(precision = 19, scale = 4)
    BigDecimal interestRate;

    @Column(nullable = false)
    @Builder.Default
    Boolean deleted = false;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

