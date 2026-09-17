package com.mediqai.admin.service;

import com.mediqai.admin.client.AuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AuthClient authClient;

    public List<Map<String, Object>> getAllUsers(String token) {
        return authClient.getAllUsers(token);
    }

    public Map<String, Object> getUserById(
            String token,
            Long id
    ) {
        return authClient.getUserById(token, id);
    }

    public Map<String, Object> updateUserStatus(
            String token,
            Long id,
            String status
    ) {
        return authClient.updateUserStatus(token, id, status);
    }

    public Map<String, Object> updateUserRole(
            String token,
            Long id,
            String role
    ) {
        return authClient.updateUserRole(token, id, role);
    }
}