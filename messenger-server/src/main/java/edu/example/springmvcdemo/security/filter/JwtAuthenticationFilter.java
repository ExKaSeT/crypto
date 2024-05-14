package edu.example.springmvcdemo.security.filter;

import edu.example.springmvcdemo.security.jwt.JwtAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import static edu.example.springmvcdemo.security.jwt.JwtProperties.JWT_ACCESS_COOKIE_NAME;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        Optional<Cookie> tokenCookieOpt = Optional.empty();
        if (nonNull(cookies)) {
            tokenCookieOpt = Arrays.stream(cookies)
                    .filter(c -> JWT_ACCESS_COOKIE_NAME.equals(c.getName()))
                    .findFirst();
        }

        if (tokenCookieOpt.isPresent()) {
            String tokenValue = tokenCookieOpt.get().getValue();
            try {
                Authentication authResult = authenticationManager.authenticate(new JwtAuthentication(tokenValue));

                SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
                context.setAuthentication(authResult);
                securityContextHolderStrategy.setContext(context);
                securityContextRepository.saveContext(context, request, response);
            } catch (AuthenticationException e) {
                securityContextHolderStrategy.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
