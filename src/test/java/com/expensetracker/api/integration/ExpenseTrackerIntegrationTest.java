package com.expensetracker.api.integration;

import com.expensetracker.api.dto.CreateExpenseRequest;
import com.expensetracker.api.dto.ExpenseResponse;
import com.expensetracker.api.dto.MonthlyReportResponse;
import com.expensetracker.api.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Disabled("Requires Docker to be running. Enable manually to run integration tests with real PostgreSQL container.")
class ExpenseTrackerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void completeUserJourney_RegisterCreateExpensesAndGenerateReportWithAlert() {
        // 1. Register user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("journey_user");
        registerRequest.setPassword("journey123");

        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                registerRequest,
                Map.class
        );

        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getBody());
        assertEquals("User registered successfully", registerResponse.getBody().get("message"));
        assertEquals("journey_user", registerResponse.getBody().get("username"));

        System.out.println("✅ Step 1: User registered successfully");

        // 2. Create category
        HttpHeaders headers = createAuthHeaders("journey_user", "journey123");

        Map<String, String> categoryRequest = Map.of("name", "Food");
        HttpEntity<Map<String, String>> categoryEntity = new HttpEntity<>(categoryRequest, headers);

        ResponseEntity<Map> categoryResponse = restTemplate.exchange(
                baseUrl + "/api/categories",
                HttpMethod.POST,
                categoryEntity,
                Map.class
        );

        assertEquals(HttpStatus.OK, categoryResponse.getStatusCode());
        assertEquals("Food", categoryResponse.getBody().get("name"));

        System.out.println("✅ Step 2: Category created successfully");

        // 3. Create multiple expenses (total should exceed 2000 to trigger alert)
        CreateExpenseRequest expense1 = new CreateExpenseRequest();
        expense1.setAmount(BigDecimal.valueOf(800.00));
        expense1.setDescription("Weekly groceries");
        expense1.setCategoryName("Food");
        expense1.setDate(LocalDate.of(2025, 11, 18));

        HttpEntity<CreateExpenseRequest> expense1Entity = new HttpEntity<>(expense1, headers);

        ResponseEntity<ExpenseResponse> expense1Response = restTemplate.exchange(
                baseUrl + "/api/expenses",
                HttpMethod.POST,
                expense1Entity,
                ExpenseResponse.class
        );

        assertEquals(HttpStatus.OK, expense1Response.getStatusCode());
        assertNotNull(expense1Response.getBody());
        assertEquals(BigDecimal.valueOf(800.00), expense1Response.getBody().getAmount());

        System.out.println("✅ Step 3a: First expense created (800.00)");

        CreateExpenseRequest expense2 = new CreateExpenseRequest();
        expense2.setAmount(BigDecimal.valueOf(900.00));
        expense2.setDescription("Restaurant dinner");
        expense2.setCategoryName("Food");
        expense2.setDate(LocalDate.of(2025, 11, 20));

        HttpEntity<CreateExpenseRequest> expense2Entity = new HttpEntity<>(expense2, headers);

        ResponseEntity<ExpenseResponse> expense2Response = restTemplate.exchange(
                baseUrl + "/api/expenses",
                HttpMethod.POST,
                expense2Entity,
                ExpenseResponse.class
        );

        assertEquals(HttpStatus.OK, expense2Response.getStatusCode());
        assertEquals(BigDecimal.valueOf(900.00), expense2Response.getBody().getAmount());

        System.out.println("✅ Step 3b: Second expense created (900.00)");

        CreateExpenseRequest expense3 = new CreateExpenseRequest();
        expense3.setAmount(BigDecimal.valueOf(700.00));
        expense3.setDescription("Bread and confectionary");
        expense3.setCategoryName("Food");
        expense3.setDate(LocalDate.of(2025, 11, 25));

        HttpEntity<CreateExpenseRequest> expense3Entity = new HttpEntity<>(expense3, headers);

        ResponseEntity<ExpenseResponse> expense3Response = restTemplate.exchange(
                baseUrl + "/api/expenses",
                HttpMethod.POST,
                expense3Entity,
                ExpenseResponse.class
        );

        assertEquals(HttpStatus.OK, expense3Response.getStatusCode());
        assertEquals(BigDecimal.valueOf(700.00), expense3Response.getBody().getAmount());

        System.out.println("✅ Step 3c: Third expense created (700.00)");

        // 4. Generate monthly report (should trigger alert: 800 + 900 + 700 = 2400 > 2000)
        HttpEntity<?> getEntity = new HttpEntity<>(headers);

        ResponseEntity<MonthlyReportResponse> reportResponse = restTemplate.exchange(
                baseUrl + "/api/expenses/report?year=2025&month=11",
                HttpMethod.GET,
                getEntity,
                MonthlyReportResponse.class
        );

        assertEquals(HttpStatus.OK, reportResponse.getStatusCode());
        assertNotNull(reportResponse.getBody());

        MonthlyReportResponse report = reportResponse.getBody();
        assertEquals(2025, report.getYear());
        assertEquals(11, report.getMonth());
        assertEquals(0, BigDecimal.valueOf(2400.00).compareTo(report.getTotalAmount()));
        assertEquals(3, report.getExpenseCount());
        assertEquals(0, BigDecimal.valueOf(2400.00).compareTo(report.getSpendingByCategory().get("Food")));

        // Verify alert triggered
        assertFalse(report.getAlerts().isEmpty(), "Alert should be triggered for spending > 2000");
        assertTrue(report.getAlerts().get(0).contains("Food"), "Alert should mention Food category");
        assertTrue(report.getAlerts().get(0).contains("2400.00"), "Alert should mention the amount");

        System.out.println("✅ Step 4: Monthly report generated with alert");
        System.out.println("   Total spending: " + report.getTotalAmount());
        System.out.println("   Alert: " + report.getAlerts().get(0));

        System.out.println("\n🎉 Complete user journey integration test passed!");
        System.out.println("   - PostgreSQL container: " + postgres.getJdbcUrl());
        System.out.println("   - Application port: " + port);
    }

    private HttpHeaders createAuthHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);
        return headers;
    }
}