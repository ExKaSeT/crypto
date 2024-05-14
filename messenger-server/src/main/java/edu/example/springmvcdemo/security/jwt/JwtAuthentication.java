package edu.example.springmvcdemo.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

public class JwtAuthentication extends AbstractAuthenticationToken {

    private final String token;
    private String username;
    private UserDetails userDetails;

    public JwtAuthentication(String token) {
        super(null);
        this.token = token;
    }

    public JwtAuthentication(String token, String username, UserDetails userDetails) {
        super(userDetails.getAuthorities());
        this.token = token;
        this.username = username;
        this.userDetails = userDetails;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return Objects.nonNull(userDetails) ? userDetails : username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
