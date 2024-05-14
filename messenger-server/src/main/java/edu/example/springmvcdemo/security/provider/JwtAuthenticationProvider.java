package edu.example.springmvcdemo.security.provider;


import edu.example.springmvcdemo.security.exception.InvalidTokenException;
import edu.example.springmvcdemo.security.jwt.JwtAuthentication;
import edu.example.springmvcdemo.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String tokenValue = (String) authentication.getCredentials();
        var payload = jwtService.parseToken(tokenValue);
        if (!payload.isAccessToken()) {
            throw new InvalidTokenException("Provided token not for access");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(payload.getUsername());
        JwtAuthentication auth = new JwtAuthentication(tokenValue, payload.getUsername(), userDetails);
        auth.setAuthenticated(true);
        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}
