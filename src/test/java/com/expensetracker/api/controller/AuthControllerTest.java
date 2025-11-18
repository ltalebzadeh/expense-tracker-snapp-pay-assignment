package com.expensetracker.api.controller;

import com.expensetracker.api.controller.exception.CustomExceptionHandler;
import com.expensetracker.api.controller.exception.DuplicateResourceException;
import com.expensetracker.api.dto.RegisterRequest;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CustomExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
    }

    @Test
    void register_Success() throws Exception {
        request.setUsername("coffee_addict");
        request.setPassword("8020");

        User user = User.builder()
                .id(1L)
                .username("coffee_addict")
                .password("encoded_password")
                .build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.username").value("coffee_addict"));
    }

    @Test
    void register_DuplicateUsername_ReturnsConflict() throws Exception {
        request.setUsername("broke_developer");
        request.setPassword("1234");

        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("Username already exists: broke_developer"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists: broke_developer"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}