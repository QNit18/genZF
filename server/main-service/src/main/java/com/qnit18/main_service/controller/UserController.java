package com.qnit18.main_service.controller;

import com.qnit18.main_service.dto.request.UserCreationRequest;
import com.qnit18.main_service.dto.request.UserUpdateRequest;
import com.qnit18.main_service.dto.response.ApiBaseResponse;
import com.qnit18.main_service.dto.response.UserResponse;
import com.qnit18.main_service.service.UserService;
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
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User Management", description = "APIs for managing users in the system")
public class UserController {
    UserService userService;

    @Operation(summary = "Create a new user", description = "Creates a new user account with a unique username")
    @PostMapping
    ApiBaseResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        log.info("Creating user: {}", request);
        ApiBaseResponse<UserResponse> response = new ApiBaseResponse<>();
        response.setResult(userService.createUser(request));
        return response;
    }

    @PutMapping("/{id}")
    ApiBaseResponse<UserResponse> updateUser(@PathVariable UUID id, @RequestBody @Valid UserUpdateRequest request) {
        log.info("Updating user: {}", id);
        ApiBaseResponse<UserResponse> response = new ApiBaseResponse<>();
        response.setResult(userService.updateUser(id, request));
        return response;
    }

    @GetMapping("/{id}")
    ApiBaseResponse<UserResponse> getUserById(@PathVariable UUID id) {
        ApiBaseResponse<UserResponse> response = new ApiBaseResponse<>();
        response.setResult(userService.getUserById(id));
        return response;
    }

    @GetMapping("/username/{username}")
    ApiBaseResponse<UserResponse> getUserByUsername(@PathVariable String username) {
        ApiBaseResponse<UserResponse> response = new ApiBaseResponse<>();
        response.setResult(userService.getUserByUsername(username));
        return response;
    }

    @GetMapping
    ApiBaseResponse<List<UserResponse>> getAllUsers() {
        ApiBaseResponse<List<UserResponse>> response = new ApiBaseResponse<>();
        response.setResult(userService.getAllUsers());
        return response;
    }

    @DeleteMapping("/{id}")
    ApiBaseResponse<String> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        ApiBaseResponse<String> response = new ApiBaseResponse<>();
        response.setResult("User has been deleted");
        return response;
    }
}

