package edu.example.springmvcdemo.security.filter;

import edu.example.springmvcdemo.dao.UserSessionRepository;
import edu.example.springmvcdemo.security.UserDetailsImpl;
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

// already have a record about user in db -> request authenticated
@RequiredArgsConstructor
public class CheckAlreadyAuthenticatedFilter extends OncePerRequestFilter {

    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    private final UserSessionRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var userList = userRepository.findAll();
        if (!userList.isEmpty()) {
            var user = userList.get(0);
            Authentication auth = new UsernamePasswordAuthenticationToken(new UserDetailsImpl(user),
                    user.getUsername(), Collections.singleton(new SimpleGrantedAuthority("USER")));
            SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(auth);
            securityContextHolderStrategy.setContext(context);
            securityContextRepository.saveContext(context, request, response);
        }
        filterChain.doFilter(request, response);
    }
}
