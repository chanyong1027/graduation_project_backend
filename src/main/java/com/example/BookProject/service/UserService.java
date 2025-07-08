package com.example.BookProject.service;

import com.example.BookProject.domain.User;
import com.example.BookProject.domain.UserDto;
import com.example.BookProject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // C: 사용자 생성
    @Transactional
    public UserDto.UserResponse createUser(UserDto.UserCreateRequest request) {
        User newUser = request.toEntity();
        User savedUser = userRepository.save(newUser);
        return new UserDto.UserResponse(savedUser);
    }

    // R: ID로 특정 사용자 조회
    @Transactional(readOnly = true)
    public UserDto.UserResponse findUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));
        return new UserDto.UserResponse(user);
    }

    // R: 모든 사용자 조회
    @Transactional(readOnly = true)
    public List<UserDto.UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto.UserResponse::new)
                .collect(Collectors.toList());
    }

    // U: 사용자 정보 수정
    @Transactional
    public UserDto.UserResponse updateUser(Long userId, UserDto.UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));
        user.updateName(request.getUserNm()); // Entity에 만든 업데이트 메소드 사용

        // user는 영속성 컨텍스트에 의해 관리되므로, 메소드 종료 시 변경 감지(dirty checking) 되어 자동 업데이트 됨
        return new UserDto.UserResponse(user);
    }

    // D: 사용자 삭제
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId);
        }
        userRepository.deleteById(userId);
    }
}
