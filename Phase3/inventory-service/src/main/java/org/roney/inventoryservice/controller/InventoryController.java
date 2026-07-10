package org.roney.inventoryservice.controller;

import java.util.Map;

import org.roney.inventoryservice.service.JwtValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final JwtValidator jwtValidator;

    public InventoryController(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkStock(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String productId,
            @RequestParam Integer quantity) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtValidator.validateAndExtractClaims(token);

            // Log verification using Java 25 console formatting
            System.out.printf("Inventory lookup authorized for user: %s%n", claims.getSubject());

            // Mock stock rule: we have everything in stock up to 100 units
            boolean available = quantity > 0 && quantity <= 100;

            return ResponseEntity.ok(Map.of(
                    "productId", productId,
                    "available", available,
                    "stockLevel", available ? 42 : 0
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Inventory check denied: " + e.getMessage());
        }
    }
}