package com.example.BookProject.controller;

import com.example.BookProject.dto.UserDto;
import com.example.BookProject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // C: 회원가입
    @PostMapping
    public ResponseEntity<UserDto.UserResponse> createUser(@RequestBody UserDto.UserCreateRequest request) {
        UserDto.UserResponse response = userService.createUser(request);
        // 생성된 리소스의 URI를 Location 헤더에 담아 201 Created 상태 코드와 함께 응답
        return ResponseEntity.created(URI.create("api/users/" + response.getUserId())).body(response);
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
