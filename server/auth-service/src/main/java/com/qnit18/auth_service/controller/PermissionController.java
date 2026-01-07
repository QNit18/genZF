package com.qnit18.auth_service.controller;

import com.qnit18.auth_service.dto.request.PermissionRequest;
import com.qnit18.auth_service.dto.response.ApiBaseResponse;
import com.qnit18.auth_service.dto.response.PermissionResponse;
import com.qnit18.auth_service.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    ApiBaseResponse<PermissionResponse> createPermission(@RequestBody PermissionRequest permissionRequest) {
        return ApiBaseResponse.<PermissionResponse>builder()
                .result(permissionService.createPermissionResponse(permissionRequest))
                .build();
    }

    @GetMapping
    ApiBaseResponse<List<PermissionResponse>> getAllPermissions() {
        return ApiBaseResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAllPermissions())
                .build();
    }

    @DeleteMapping("/{permissionName}")
    ApiBaseResponse<Void> deletePermission(@PathVariable String permissionName) {
        permissionService.deletePermission(permissionName);
        return ApiBaseResponse.<Void>builder().build();
    }

}
