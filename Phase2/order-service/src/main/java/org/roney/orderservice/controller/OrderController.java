package org.roney.orderservice.controller;

import io.jsonwebtoken.Claims;
import org.roney.orderservice.service.JwtValidator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final JwtValidator jwtValidator;
    private final RestClient restClient;

    public OrderController(JwtValidator jwtValidator, RestClient restClient) {
        this.jwtValidator = jwtValidator;
        this.restClient = restClient;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> orderDetails) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            // Validate token using local secrets
            Claims claims = jwtValidator.validateAndExtractClaims(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // Print verification details using modern Java 25 string templates/formatting
            System.out.printf("Validated request from user: %s with role: %s%n", username, role);

            // Extract order details
            String productId = (String) orderDetails.get("productId");
            Object quantityObj = orderDetails.get("quantity");
            if (productId == null || quantityObj == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing productId or quantity");
            }
            Integer quantity = (quantityObj instanceof Number) ? ((Number) quantityObj).intValue() : Integer.parseInt(quantityObj.toString());

            // Call Inventory Service via RestClient
            Map<String, Object> inventoryResponse = restClient.get()
                    .uri("/check?productId={productId}&quantity={quantity}",
                     productId, quantity)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (inventoryResponse == null || !Boolean.TRUE.equals(inventoryResponse.get("available"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "status", "ORDER_FAILED",
                        "reason", "Requested quantity is unavailable or out of stock"
                ));
            }

            // Mock saving order
            return ResponseEntity.ok(Map.of(
                    "status", "ORDER_CREATED",
                    "user", username,
                    "item", productId,
                    "quantity", quantity,
                    "stockLevel", inventoryResponse.get("stockLevel")
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token verification or stock check failed: " + e.getMessage());
        }
    }
}