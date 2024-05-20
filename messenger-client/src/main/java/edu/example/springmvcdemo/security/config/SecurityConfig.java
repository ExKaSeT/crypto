package edu.example.springmvcdemo.security.config;

import edu.example.springmvcdemo.dao.UserSessionRepository;
import edu.example.springmvcdemo.security.filter.CheckAlreadyAuthenticatedFilter;
import edu.example.springmvcdemo.security.provider.ClientAuthenticationProvider;
import edu.example.springmvcdemo.service.UserSessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           LogoutSuccessHandler logoutHandler,
                                           UserSessionRepository userSessionRepository) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/login", "/register", "/static/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .addFilterBefore(new CheckAlreadyAuthenticatedFilter(userSessionRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .formLogin(formLogin -> {
                    formLogin
                            .usernameParameter("username")
                            .passwordParameter("password")
                            .loginPage("/login")
                            .defaultSuccessUrl("/");
                })
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutHandler));

        return http.build();
    }

    @Bean
    public AuthenticationProvider clientAuthenticationProvider(UserSessionService userSessionService) {
        return new ClientAuthenticationProvider(userSessionService);
    }
}