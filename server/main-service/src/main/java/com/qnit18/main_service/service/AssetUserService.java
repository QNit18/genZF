package com.qnit18.main_service.service;

import com.qnit18.main_service.dto.request.AssetUserCreationRequest;
import com.qnit18.main_service.dto.request.AssetUserUpdateRequest;
import com.qnit18.main_service.dto.response.AssetUserResponse;
import com.qnit18.main_service.entity.AssetUser;
import com.qnit18.main_service.entity.Portfolio;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import com.qnit18.main_service.mapper.AssetUserMapper;
import com.qnit18.main_service.repository.AssetUserRepository;
import com.qnit18.main_service.repository.PortfolioRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssetUserService {
    AssetUserRepository assetUserRepository;
    PortfolioRepository portfolioRepository;
    AssetUserMapper assetUserMapper;

    @Transactional
    public AssetUserResponse createAssetUser(AssetUserCreationRequest request) {
        Portfolio portfolio = portfolioRepository.findById(request.getPortfolioId())
                .orElseThrow(() -> new AppException(ErrorCode.PORTFOLIO_NOT_FOUND));

        AssetUser assetUser = assetUserMapper.toAssetUser(request);
        assetUser.setPortfolio(portfolio);
        return assetUserMapper.toAssetUserResponse(assetUserRepository.save(assetUser));
    }

    @Transactional
    public AssetUserResponse updateAssetUser(UUID id, AssetUserUpdateRequest request) {
        AssetUser assetUser = assetUserRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_USER_NOT_FOUND));

        assetUserMapper.updateAssetUser(assetUser, request);
        return assetUserMapper.toAssetUserResponse(assetUserRepository.save(assetUser));
    }

    @Transactional(readOnly = true)
    public AssetUserResponse getAssetUserById(UUID id) {
        AssetUser assetUser = assetUserRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_USER_NOT_FOUND));
        return assetUserMapper.toAssetUserResponse(assetUser);
    }

    @Transactional(readOnly = true)
    public List<AssetUserResponse> getAssetUsersByPortfolioId(UUID portfolioId) {
        return assetUserRepository.findByPortfolioId(portfolioId).stream()
                .map(assetUserMapper::toAssetUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetUserResponse> getActiveAssetUsersByPortfolioId(UUID portfolioId) {
        return assetUserRepository.findByPortfolioIdAndDeletedFalse(portfolioId).stream()
                .map(assetUserMapper::toAssetUserResponse)
                .toList();
    }

    @Transactional
    public void deleteAssetUser(UUID id) {
        AssetUser assetUser = assetUserRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_USER_NOT_FOUND));
        
        assetUser.setDeleted(true);
        assetUserRepository.save(assetUser);
    }

    @Transactional
    public void hardDeleteAssetUser(UUID id) {
        if (!assetUserRepository.existsById(id)) {
            throw new AppException(ErrorCode.ASSET_USER_NOT_FOUND);
        }
        assetUserRepository.deleteById(id);
    }
}

