package com.expensetracker.api.controller;

import com.expensetracker.api.dto.RegisterRequest;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return Map.of("message", "User registered successfully", "username", user.getUsername());
    }
}