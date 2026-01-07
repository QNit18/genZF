package com.qnit18.main_service.repository;

import com.qnit18.main_service.entity.AssetUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetUserRepository extends JpaRepository<AssetUser, UUID> {
    List<AssetUser> findByPortfolioId(UUID portfolioId);
    List<AssetUser> findByPortfolioIdAndDeletedFalse(UUID portfolioId);
    Optional<AssetUser> findByPortfolioIdAndAssetNameAndDeletedFalse(UUID portfolioId, String assetName);
}

