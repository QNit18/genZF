package com.qnit18.auth_service.service;

import com.qnit18.auth_service.dto.request.PermissionRequest;
import com.qnit18.auth_service.dto.response.PermissionResponse;
import com.qnit18.auth_service.entity.Permission;
import com.qnit18.auth_service.mapper.PermissionMapper;
import com.qnit18.auth_service.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse createPermissionResponse(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        log.info("Permission created with name: {}", permission.getName());
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        log.info("Fetched {} permissions", permissions.size());
        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();
    }

    public void deletePermission(String permissionName) {
        permissionRepository.deleteById(permissionName);
        log.info("Permission deleted with name: {}", permissionName);
    }
}
