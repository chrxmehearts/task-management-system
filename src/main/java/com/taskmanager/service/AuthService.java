package com.taskmanager.service;

import com.taskmanager.dto.request.LoginRequestDto;
import com.taskmanager.dto.request.RegisterRequestDto;
import com.taskmanager.dto.response.AuthResponseDto;
import com.taskmanager.dto.response.UserResponseDto;

public interface AuthService {

    UserResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);
}
