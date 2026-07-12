package com.dealership.security;

import com.dealership.user.User;
import com.dealership.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads user-specific data for Spring Security authentication.
 *
 * Spring Security calls loadUserByUsername() during JWT filter validation
 * to fetch the current user from the database and verify they still exist.
 *
 * The "username" in Spring Security context = user's email in our system.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by email (Spring Security calls this as "username").
     *
     * @param email the user's email address
     * @return UserDetails with email, hashed password, and role as authority
     * @throws UsernameNotFoundException if no user with this email exists
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        // Convert our Role enum to Spring Security GrantedAuthority
        // Prefix "ROLE_" is required by Spring Security's hasRole() checks
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}
