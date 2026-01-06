package com.qnit18.auth_service.controller;

import com.qnit18.auth_service.dto.request.UserCreationRequest;
import com.qnit18.auth_service.dto.request.UserUpdateRequest;
import com.qnit18.auth_service.dto.response.ApiBaseResponse;
import com.qnit18.auth_service.dto.response.UserResponse;
import com.qnit18.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    ApiBaseResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){
        ApiBaseResponse<UserResponse> response = new ApiBaseResponse<>();
        response.setResult(userService.createUser(request));
        return response;
    }

    @PutMapping("/{userId}")
    UserResponse updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request){
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return "User has been deleted";
    }

    @GetMapping("/{userId}")
    UserResponse getUser(@PathVariable("userId") String userId){
        return userService.getUser(userId);
    }

    @GetMapping("/my-info")
    ApiBaseResponse<UserResponse> getMyInfo() {
        return ApiBaseResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @GetMapping
    List<UserResponse> getUsers(){
        return userService.getUsers();
    }

}
