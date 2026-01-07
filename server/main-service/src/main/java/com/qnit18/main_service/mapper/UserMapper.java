package com.qnit18.main_service.mapper;

import com.qnit18.main_service.dto.request.UserCreationRequest;
import com.qnit18.main_service.dto.request.UserUpdateRequest;
import com.qnit18.main_service.dto.response.UserResponse;
import com.qnit18.main_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}

