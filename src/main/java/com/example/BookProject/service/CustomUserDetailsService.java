package com.example.BookProject.service;

import com.example.BookProject.domain.User;
import com.example.BookProject.repository.UserRepository;
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
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        // userEmail로 사용자를 찾고, 없으면 예외 발생
        return userRepository.findByUserEmail(userEmail) // userEmail로 찾는 메서드가 필요
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
    }
}