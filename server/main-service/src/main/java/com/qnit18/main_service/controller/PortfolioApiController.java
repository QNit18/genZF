package com.qnit18.main_service.controller;

import com.qnit18.main_service.dto.request.PortfolioTransactionRequest;
import com.qnit18.main_service.dto.response.*;
import com.qnit18.main_service.service.PortfolioService;
import com.qnit18.main_service.util.JwtUtil;
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
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Portfolio API", description = "Frontend APIs for portfolio management")
public class PortfolioApiController {
    PortfolioService portfolioService;
    JwtUtil jwtUtil;

    @Operation(summary = "Get portfolio assets", description = "Get all assets in the authenticated user's portfolio")
    @GetMapping("/assets")
    ApiBaseResponse<List<AssetUserResponse>> getPortfolioAssets() {
        String userId = jwtUtil.getUserIdFromToken();
        if (userId == null) {
            throw new com.qnit18.main_service.exception.AppException(com.qnit18.main_service.exception.ErrorCode.USER_NOT_FOUND);
        }
        ApiBaseResponse<List<AssetUserResponse>> response = new ApiBaseResponse<>();
        response.setResult(portfolioService.getPortfolioAssets(userId));
        return response;
    }

    @Operation(summary = "Get portfolio allocation", description = "Get allocation breakdown by asset and category for authenticated user's portfolio")
    @GetMapping("/allocation")
    ApiBaseResponse<AllocationResponse> getPortfolioAllocation() {
        String userId = jwtUtil.getUserIdFromToken();
        if (userId == null) {
            throw new com.qnit18.main_service.exception.AppException(com.qnit18.main_service.exception.ErrorCode.USER_NOT_FOUND);
        }
        ApiBaseResponse<AllocationResponse> response = new ApiBaseResponse<>();
        response.setResult(portfolioService.getPortfolioAllocation(userId));
        return response;
    }

    @Operation(summary = "Add or update transaction", description = "Add a new transaction or update existing one (upsert by assetName)")
    @PostMapping("/transaction/add")
    ApiBaseResponse<AssetUserResponse> addTransaction(@RequestBody @Valid PortfolioTransactionRequest request) {
        String userId = jwtUtil.getUserIdFromToken();
        if (userId == null) {
            throw new com.qnit18.main_service.exception.AppException(com.qnit18.main_service.exception.ErrorCode.USER_NOT_FOUND);
        }
        log.info("Adding/updating transaction for user: {}", userId);
        ApiBaseResponse<AssetUserResponse> response = new ApiBaseResponse<>();
        response.setResult(portfolioService.addOrUpdateTransaction(userId, request));
        return response;
    }

    @Operation(summary = "Delete transaction", description = "Delete a transaction from authenticated user's portfolio")
    @DeleteMapping("/delete/{id}")
    ApiBaseResponse<String> deleteTransaction(@PathVariable UUID id) {
        String userId = jwtUtil.getUserIdFromToken();
        if (userId == null) {
            throw new com.qnit18.main_service.exception.AppException(com.qnit18.main_service.exception.ErrorCode.USER_NOT_FOUND);
        }
        log.info("Deleting transaction {} for user: {}", id, userId);
        portfolioService.deleteTransaction(userId, id);
        ApiBaseResponse<String> response = new ApiBaseResponse<>();
        response.setResult("Transaction has been deleted");
        return response;
    }
}

