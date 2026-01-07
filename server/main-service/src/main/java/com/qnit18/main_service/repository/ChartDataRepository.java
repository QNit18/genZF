package com.qnit18.main_service.repository;

import com.qnit18.main_service.constant.Timeframe;
import com.qnit18.main_service.entity.ChartData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChartDataRepository extends JpaRepository<ChartData, UUID> {
    List<ChartData> findByAssetId(UUID assetId);
    Optional<ChartData> findByAssetIdAndTimeframe(UUID assetId, Timeframe timeframe);
}

