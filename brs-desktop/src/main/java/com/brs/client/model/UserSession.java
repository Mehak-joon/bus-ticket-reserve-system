package com.brs.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String token;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equalsIgnoreCase(role);
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private String token;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public UserSession build() { return new UserSession(id, username, email, fullName, role, token); }
    }
}
