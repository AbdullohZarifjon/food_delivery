package com.example.food.security;

import com.example.food.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        if (login.matches("^\\+?\\d{9,15}$")) {
            // normalize(login) yopildi, keyin findByPhoneNumber yopilishi kerak
            return userRepository.findByPhoneNumber(normalize(login))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + login));
        }
        return userRepository.findByUsername(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + login));
    }

    private String normalize(String phone) {
        return phone.replaceAll("[^+0-9]", "");
    }
}
