package com.aigrading.filter;

import com.aigrading.util.JwtUtil;
import com.aigrading.util.JwtUtil.JwtClaims;

/**
 * Filter for intercepting HTTP requests, verifying JWT tokens in Authorization headers,
 * and performing Role-Based Access Control (RBAC).
 */
public class JwtAuthFilter {

    public static class AuthContext {
        public boolean isAuthenticated;
        public JwtClaims claims;
        public String failureReason;

        public AuthContext(boolean isAuthenticated, JwtClaims claims, String failureReason) {
            this.isAuthenticated = isAuthenticated;
            this.claims = claims;
            this.failureReason = failureReason;
        }
    }

    public static AuthContext authenticate(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return new AuthContext(false, null, "Missing Authorization header");
        }

        String token = authHeader.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        JwtClaims claims = JwtUtil.parseAndVerify(token);
        if (claims == null) {
            return new AuthContext(false, null, "Invalid or expired JWT token");
        }

        return new AuthContext(true, claims, null);
    }

    public static boolean checkRole(AuthContext context, String... allowedRoles) {
        if (context == null || !context.isAuthenticated || context.claims == null) {
            return false;
        }
        String userRole = context.claims.role;
        for (String role : allowedRoles) {
            if (role.equalsIgnoreCase(userRole)) {
                return true;
            }
        }
        return false;
    }
}
