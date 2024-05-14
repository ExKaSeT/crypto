package edu.example.springmvcdemo.security;


import edu.example.springmvcdemo.dao.UserRepository;
import edu.example.springmvcdemo.model.Role;
import edu.example.springmvcdemo.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.Objects.isNull;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Unable to find user with username: " + username));

        if (isNull(user.getRole())) {
            user.setRole(Role.USER);
        }

        Set<GrantedAuthority> roles = user.getRole().getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
        return new UserDetailsImpl(user, roles);
    }
}
