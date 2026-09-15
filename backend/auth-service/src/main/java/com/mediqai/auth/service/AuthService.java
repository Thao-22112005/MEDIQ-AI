package com.mediqai.auth.service;

import com.mediqai.auth.dto.request.LoginRequest;
import com.mediqai.auth.dto.request.RegisterRequest;
import com.mediqai.auth.dto.response.LoginResponse;
import com.mediqai.auth.dto.response.RegisterResponse;
import com.mediqai.auth.dto.response.UserResponse;
import com.mediqai.auth.entity.Role;
import com.mediqai.auth.entity.User;
import com.mediqai.auth.exception.BadRequestException;
import com.mediqai.auth.exception.DuplicateResourceException;
import com.mediqai.auth.repository.UserRepository;
import com.mediqai.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ==============================
    // REGISTER
    // ==============================

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email đã được sử dụng"
            );
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException(
                    "Số điện thoại đã được sử dụng"
            );
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(
                        passwordEncoder.encode(request.getPassword())
                )
                .role(Role.PATIENT)
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .status(savedUser.getStatus())
                .build();
    }

    // ==============================
    // LOGIN
    // ==============================

    public LoginResponse login(LoginRequest request) {

        // Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Email hoặc mật khẩu không đúng"
                        )
                );

        // Kiểm tra password bằng BCrypt
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new BadRequestException(
                    "Email hoặc mật khẩu không đúng"
            );
        }

        // Kiểm tra trạng thái tài khoản
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException(
                    "Tài khoản đã bị khóa hoặc không hoạt động"
            );
        }

        // Tạo JWT
        String accessToken = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return LoginResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus())
                .accessToken(accessToken)
                .build();
    }

    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Không tìm thấy tài khoản"
                        )
                );

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus())
                .build();
    }
}