package edu.example.springmvcdemo.security.provider;

import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@RequiredArgsConstructor
public class ClientAuthenticationProvider implements AuthenticationProvider {

    private final UserSessionService userSessionService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var token = (UsernamePasswordAuthenticationToken) authentication;
        var username = (String) token.getPrincipal();
        var password = (String) token.getCredentials();

        var user = userSessionService.loginRegister(username, password, false);

        return new UsernamePasswordAuthenticationToken(new SimpleGrantedAuthority("USER"),
                new UserDetailsImpl(user),
                Collections.singletonList(new SimpleGrantedAuthority("USER")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
