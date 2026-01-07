package com.qnit18.main_service.repository;

import com.qnit18.main_service.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {
    Optional<Asset> findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);
    List<Asset> findByAssetHomeTrueOrderByChangePercentageDesc();
}

