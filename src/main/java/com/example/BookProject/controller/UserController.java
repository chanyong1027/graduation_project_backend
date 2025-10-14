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
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> login(@RequestBody UserDto.LoginRequest request) {
        // AuthenticationManager를 사용하여 사용자 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserEmail(), request.getUserPw())
        );

        // 인증 성공 후 SecurityContextHolder에 인증 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 사용자 정보 가져오기
        String userEmail = authentication.getName();
        UserDto.UserResponse userResponse = userService.findUserByEmail(userEmail);

        // JWT 토큰 생성
        String jwtToken = jwtTokenProvider.createToken(authentication);

        // 풍부한 정보를 담은 응답 생성
        UserDto.LoginResponse loginResponse = new UserDto.LoginResponse(
                jwtToken,
                userResponse.getUserId(),
                userResponse.getUserNm(),
                userResponse.getUserEmail()
        );

        return ResponseEntity.ok(loginResponse);
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

    // 중복 확인 API 추가
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam("email") String email) {
        boolean exists = userService.checkEmailExists(email);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUserName(@RequestParam("username") String username) {
        boolean exists = userService.checkUserNmExists(username);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }
}
