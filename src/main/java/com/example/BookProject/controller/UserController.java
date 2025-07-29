package com.example.BookProject.controller;

import com.example.BookProject.dto.UserDto;
import com.example.BookProject.jwt.JwtTokenProvider;
import com.example.BookProject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // C: 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserDto.UserResponse> createUser(@RequestBody UserDto.UserCreateRequest request) {
        UserDto.UserResponse response = userService.createUser(request);
        // 생성된 리소스의 URI를 Location 헤더에 담아 201 Created 상태 코드와 함께 응답
        return ResponseEntity.created(URI.create("api/users/" + response.getUserId())).body(response);
    }

    // 로그인 (JWT 토큰 발급은 아직 미구현)
    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> login(@RequestBody UserDto.LoginRequest request) {
        // AuthenticationManager를 사용하여 사용자 인증 시도
        // CustomUserDetailsService의 loadUserByUsername이 호출되어 사용자 정보 로드 및 비밀번호 비교
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserEmail(), request.getUserPw())
        );

        // 인증 성공 후 JWT 토큰 생성
        String jwtToken = jwtTokenProvider.createToken(authentication);

        // 인증 성공 시 SecurityContextHolder에 인증 객체 저장 (선택 사항, JWT 사용 시 주로 토큰 발급)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ResponseEntity.ok(new UserDto.LoginResponse(jwtToken));
    }

    // R: 모든 회원 조회
    @GetMapping
    public ResponseEntity<List<UserDto.UserResponse>> getAllUsers(){
        List<UserDto.UserResponse> responses = userService.findAllUsers();
        return ResponseEntity.ok(responses);
    }

    // R: 특정 회원 조회
    @GetMapping("/{id}")
    public  ResponseEntity<UserDto.UserResponse> getUserById(@PathVariable("id") Long userId){
        UserDto.UserResponse response = userService.findUserById(userId);
        return ResponseEntity.ok(response);
    }

    // U: 회원 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<UserDto.UserResponse> updateUser(@PathVariable("id") Long userId, @RequestBody UserDto.UserUpdateRequest request) {
        UserDto.UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    // D: 회원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto.UserResponse> deleteUser(@PathVariable("id") Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
