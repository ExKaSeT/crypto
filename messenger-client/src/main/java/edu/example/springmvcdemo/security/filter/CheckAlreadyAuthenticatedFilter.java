package edu.example.springmvcdemo.security.filter;

import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.UserSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

import static java.util.Objects.nonNull;

// already have a record about user in db -> request authenticated
@RequiredArgsConstructor
public class CheckAlreadyAuthenticatedFilter extends OncePerRequestFilter {

    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    private final UserSessionService userSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var user = userSessionService.getUser();
        if (nonNull(user)) {
            if (userSessionService.isUserLoggedIn()) {
                Authentication auth = new UsernamePasswordAuthenticationToken(new UserDetailsImpl(user),
                        user.getUsername(), Collections.singleton(new SimpleGrantedAuthority("USER")));
                SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
                context.setAuthentication(auth);
                securityContextHolderStrategy.setContext(context);
                securityContextRepository.saveContext(context, request, response);
            }
        }
        filterChain.doFilter(request, response);
    }
}
