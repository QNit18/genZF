package com.qnit18.auth_service.mapper;


import com.qnit18.auth_service.dto.request.PermissionRequest;
import com.qnit18.auth_service.dto.response.PermissionResponse;
import com.qnit18.auth_service.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest permissionRequest);
    PermissionResponse toPermissionResponse(Permission permission);
}
