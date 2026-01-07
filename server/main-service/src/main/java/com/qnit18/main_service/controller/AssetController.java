package com.qnit18.main_service.controller;

import com.qnit18.main_service.dto.request.AssetCreationRequest;
import com.qnit18.main_service.dto.request.AssetUpdateRequest;
import com.qnit18.main_service.dto.response.ApiBaseResponse;
import com.qnit18.main_service.dto.response.AssetResponse;
import com.qnit18.main_service.service.AssetService;
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
@RequestMapping("/assets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Asset Management", description = "APIs for managing trading assets (Gold, Bitcoin, Forex, etc.)")
public class AssetController {
    AssetService assetService;

    @Operation(summary = "Create a new asset", description = "Creates a new trading asset with real-time price data")
    @PostMapping
    ApiBaseResponse<AssetResponse> createAsset(@RequestBody @Valid AssetCreationRequest request) {
        log.info("Creating asset: {}", request);
        ApiBaseResponse<AssetResponse> response = new ApiBaseResponse<>();
        response.setResult(assetService.createAsset(request));
        return response;
    }

    @Operation(summary = "Update an asset", description = "Updates asset information including price and market data")
    @PutMapping("/{id}")
    ApiBaseResponse<AssetResponse> updateAsset(@PathVariable UUID id, @RequestBody @Valid AssetUpdateRequest request) {
        log.info("Updating asset: {}", id);
        ApiBaseResponse<AssetResponse> response = new ApiBaseResponse<>();
        response.setResult(assetService.updateAsset(id, request));
        return response;
    }

    @Operation(summary = "Update asset price", description = "Updates the current price and recalculates change percentage and high/low values")
    @PutMapping("/{id}/price")
    ApiBaseResponse<AssetResponse> updatePrice(@PathVariable UUID id, @RequestBody @Valid AssetUpdateRequest request) {
        log.info("Updating asset price: {}", id);
        ApiBaseResponse<AssetResponse> response = new ApiBaseResponse<>();
        response.setResult(assetService.updatePrice(id, request));
        return response;
    }

    @Operation(summary = "Get asset by ID", description = "Retrieves asset details by its unique identifier")
    @GetMapping("/{id}")
    ApiBaseResponse<AssetResponse> getAssetById(@PathVariable UUID id) {
        ApiBaseResponse<AssetResponse> response = new ApiBaseResponse<>();
        response.setResult(assetService.getAssetById(id));
        return response;
    }

    @Operation(summary = "Get asset by symbol", description = "Retrieves asset details by trading symbol (e.g., XAU/USD, BTC/USD)")
    @GetMapping("/symbol/{symbol}")
    ApiBaseResponse<AssetResponse> getAssetBySymbol(@PathVariable String symbol) {
        ApiBaseResponse<AssetResponse> response = new ApiBaseResponse<>();
        response.setResult(assetService.getAssetBySymbol(symbol));
        return response;
    }

    @Operation(summary = "Get all assets", description = "Retrieves a list of all available trading assets")
    @GetMapping
    ApiBaseResponse<List<AssetResponse>> getAllAssets() {
        ApiBaseResponse<List<AssetResponse>> response = new ApiBaseResponse<>();
        response.setResult(assetService.getAllAssets());
        return response;
    }

    @Operation(summary = "Delete an asset", description = "Permanently deletes an asset from the system")
    @DeleteMapping("/{id}")
    ApiBaseResponse<String> deleteAsset(@PathVariable UUID id) {
        assetService.deleteAsset(id);
        ApiBaseResponse<String> response = new ApiBaseResponse<>();
        response.setResult("Asset has been deleted");
        return response;
    }
}

