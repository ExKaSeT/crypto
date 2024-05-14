package edu.example.springmvcdemo.security.provider;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DbAuthenticationProvider extends DaoAuthenticationProvider {

    public DbAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        setPasswordEncoder(passwordEncoder);
        setUserDetailsService(userDetailsService);
    }
}
