package com.example.becommerce.security;

import com.example.becommerce.entity.User;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user from DB for Spring Security authentication pipeline.
 * Used by JwtAuthenticationFilter and DaoAuthenticationProvider.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load by email — the value stored as "username" in JWT subject.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy người dùng với email: " + email));
        return new CustomUserDetails(user);
    }
}
