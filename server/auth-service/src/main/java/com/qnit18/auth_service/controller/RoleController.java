package com.qnit18.auth_service.controller;

import com.qnit18.auth_service.dto.request.RoleRequest;
import com.qnit18.auth_service.dto.response.ApiBaseResponse;
import com.qnit18.auth_service.dto.response.RoleResponse;
import com.qnit18.auth_service.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiBaseResponse<RoleResponse> create(@RequestBody RoleRequest request){
        return ApiBaseResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    ApiBaseResponse<List<RoleResponse>> getAll(){
        return ApiBaseResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{role}")
    ApiBaseResponse<Void> delete(@PathVariable String role){
        roleService.delete(role);
        return ApiBaseResponse.<Void>builder().build();
    }

}
