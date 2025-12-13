package com.preschool.preschool.service;

import com.preschool.preschool.entity.User;
import com.preschool.preschool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = username.trim().toLowerCase();
        System.out.println("Attempting to login user: " + normalizedUsername + " (Original: " + username + ")");
        User user = userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + username);
                    return new UsernameNotFoundException("User not found");
                });

        System.out.println("User found: " + user.getUsername());
        System.out.println("Stored Password Hash: " + user.getPassword());
        System.out.println("Role: " + user.getRole());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
