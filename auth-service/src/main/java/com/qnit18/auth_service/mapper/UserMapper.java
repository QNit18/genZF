package com.qnit18.auth_service.mapper;

import com.qnit18.auth_service.dto.request.UserCreationRequest;
import com.qnit18.auth_service.dto.request.UserUpdateRequest;
import com.qnit18.auth_service.dto.response.UserResponse;
import com.qnit18.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
