package com.mediqai.auth.service;

import com.mediqai.auth.dto.request.*;
import com.mediqai.auth.dto.response.LoginResponse;
import com.mediqai.auth.dto.response.RegisterResponse;
import com.mediqai.auth.dto.response.UserResponse;
import com.mediqai.auth.entity.OtpType;
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
    private final OtpService otpService;

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
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role(Role.PATIENT)
                .status("PENDING")
                .build();

        User savedUser = userRepository.save(user);

        otpService.createAndSendOtp(
                savedUser,
                OtpType.REGISTER
        );

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

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Email hoặc mật khẩu không đúng"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new BadRequestException(
                    "Email hoặc mật khẩu không đúng"
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException(
                    "Tài khoản đã bị khóa hoặc không hoạt động"
            );
        }

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
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    // ==============================
    // GET CURRENT USER
    // ==============================

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

    // ==============================
    // VERIFY REGISTER OTP
    // ==============================

    public void verifyRegisterOtp(
            VerifyOtpRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Tài khoản không tồn tại"
                        )
                );

        if ("ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException(
                    "Tài khoản đã được xác thực"
            );
        }

        otpService.verifyOtp(
                user,
                request.getOtp(),
                OtpType.REGISTER
        );

        user.setStatus("ACTIVE");

        userRepository.save(user);
    }

    // ==============================
    // FORGOT PASSWORD
    // ==============================

    public void forgotPassword(
            ForgotPasswordRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        // Không tiết lộ email có tồn tại hay không
        if (user == null) {
            return;
        }

        otpService.createAndSendOtp(
                user,
                OtpType.FORGOT_PASSWORD
        );
    }

    // ==============================
    // VERIFY FORGOT PASSWORD OTP
    // ==============================

    public void verifyForgotPasswordOtp(
            VerifyOtpRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "OTP không hợp lệ"
                        )
                );

        otpService.verifyOtp(
                user,
                request.getOtp(),
                OtpType.FORGOT_PASSWORD
        );
    }

    // ==============================
    // RESET PASSWORD
    // ==============================

    public void resetPassword(
            ResetPasswordRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Không tìm thấy tài khoản"
                        )
                );

        // Bắt buộc OTP FORGOT_PASSWORD
        // phải được xác thực trước khi đổi password
        if (!otpService.isOtpVerified(
                user,
                OtpType.FORGOT_PASSWORD
        )) {
            throw new BadRequestException(
                    "Bạn chưa xác thực OTP hoặc OTP đã hết hiệu lực"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        // Không cho dùng lại OTP sau khi reset password
        otpService.consumeVerifiedOtp(
                user,
                OtpType.FORGOT_PASSWORD
        );
    }

    public void resendRegisterOtp(
            ResendOtpRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Tài khoản không tồn tại"
                        )
                );

        if ("ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException(
                    "Tài khoản đã được xác thực"
            );
        }

        otpService.createAndSendOtp(
                user,
                OtpType.REGISTER
        );
    }

    public void changePassword(String email, ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash())) {

            throw new BadRequestException(
                    "Mật khẩu hiện tại không chính xác");
        }

        if (request.getCurrentPassword()
                .equals(request.getNewPassword())) {

            throw new BadRequestException(
                    "Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }

    public UserResponse updateProfile(
            String currentEmail,
            UpdateProfileRequest request) {

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() ->
                        new BadRequestException("Không tìm thấy tài khoản"));

        if (!user.getPhone().equals(request.getPhone())
                && userRepository.existsByPhone(request.getPhone())) {

            throw new DuplicateResourceException(
                    "Số điện thoại đã được sử dụng");
        }

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAvatarUrl(request.getAvatarUrl());

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .status(savedUser.getStatus())
                .avatarUrl(savedUser.getAvatarUrl())
                .build();
    }
}