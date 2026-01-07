package com.qnit18.main_service.controller;

import com.qnit18.main_service.dto.request.AssetUserCreationRequest;
import com.qnit18.main_service.dto.request.AssetUserUpdateRequest;
import com.qnit18.main_service.dto.response.ApiBaseResponse;
import com.qnit18.main_service.dto.response.AssetUserResponse;
import com.qnit18.main_service.service.AssetUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/asset-users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Asset Holdings Management", description = "APIs for managing user asset holdings in portfolios")
public class AssetUserController {
    AssetUserService assetUserService;

    @PostMapping
    ApiBaseResponse<AssetUserResponse> createAssetUser(@RequestBody @Valid AssetUserCreationRequest request) {
        log.info("Creating asset user: {}", request);
        ApiBaseResponse<AssetUserResponse> response = new ApiBaseResponse<>();
        response.setResult(assetUserService.createAssetUser(request));
        return response;
    }

    @PutMapping("/{id}")
    ApiBaseResponse<AssetUserResponse> updateAssetUser(@PathVariable UUID id, @RequestBody @Valid AssetUserUpdateRequest request) {
        log.info("Updating asset user: {}", id);
        ApiBaseResponse<AssetUserResponse> response = new ApiBaseResponse<>();
        response.setResult(assetUserService.updateAssetUser(id, request));
        return response;
    }

    @GetMapping("/{id}")
    ApiBaseResponse<AssetUserResponse> getAssetUserById(@PathVariable UUID id) {
        ApiBaseResponse<AssetUserResponse> response = new ApiBaseResponse<>();
        response.setResult(assetUserService.getAssetUserById(id));
        return response;
    }

    @GetMapping("/portfolio/{portfolioId}")
    ApiBaseResponse<List<AssetUserResponse>> getAssetUsersByPortfolioId(@PathVariable UUID portfolioId) {
        ApiBaseResponse<List<AssetUserResponse>> response = new ApiBaseResponse<>();
        response.setResult(assetUserService.getAssetUsersByPortfolioId(portfolioId));
        return response;
    }

    @GetMapping("/portfolio/{portfolioId}/active")
    ApiBaseResponse<List<AssetUserResponse>> getActiveAssetUsersByPortfolioId(@PathVariable UUID portfolioId) {
        ApiBaseResponse<List<AssetUserResponse>> response = new ApiBaseResponse<>();
        response.setResult(assetUserService.getActiveAssetUsersByPortfolioId(portfolioId));
        return response;
    }

    @DeleteMapping("/{id}")
    ApiBaseResponse<String> deleteAssetUser(@PathVariable UUID id) {
        assetUserService.deleteAssetUser(id);
        ApiBaseResponse<String> response = new ApiBaseResponse<>();
        response.setResult("Asset user has been deleted");
        return response;
    }
}

