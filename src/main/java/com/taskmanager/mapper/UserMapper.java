package com.taskmanager.mapper;

import com.taskmanager.dto.response.UserResponseDto;
import com.taskmanager.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toResponseDto(User user);
}
