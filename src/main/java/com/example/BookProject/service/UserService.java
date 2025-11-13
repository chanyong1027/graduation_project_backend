package com.example.BookProject.service;

import com.example.BookProject.domain.AgeGroup;
import com.example.BookProject.domain.Gender;
import com.example.BookProject.domain.User;
import com.example.BookProject.dto.UserDto;
import com.example.BookProject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // C: 사용자 생성
    @Transactional
    public UserDto.UserResponse createUser(UserDto.UserCreateRequest request) {
        // 이메일 중복 확인
        if(userRepository.existsByUserEmail(request.getUserEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        // 닉네임 중복 확인
        if(userRepository.existsByUserNm(request.getUserNm())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        //비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getUserPw());
        // Gender enum 변환 (optional)
        Gender gender = null;
        if (request.getGender() != null && !request.getGender().isEmpty()) {
            try {
                gender = Gender.valueOf(request.getGender());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 성별입니다: " + request.getGender());
            }
        }
        // AgeGroup enum 변환 (optional)
        AgeGroup ageGroup = null;
        if (request.getAgeGroup() != null && !request.getAgeGroup().isEmpty()) {
            try {
                ageGroup = AgeGroup.valueOf(request.getAgeGroup());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("유효하지 않은 연령대입니다: " + request.getAgeGroup());
            }
        }
        User newUser = User.createUser(request.getUserEmail(), encodedPassword, request.getUserNm(), gender, ageGroup);
        User savedUser = userRepository.save(newUser);
        return new UserDto.UserResponse(savedUser);
    }

    // R: ID로 특정 사용자 조회
    @Transactional(readOnly = true)
    public UserDto.UserResponse findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));
        return new UserDto.UserResponse(user);
    }

    // R: email로 특정 사용자 조회
    @Transactional(readOnly = true)
    public UserDto.UserResponse findUserByEmail(String userEmail) {
        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userEmail));
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
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));
        user.updateName(request.getUserNm()); // Entity에 만든 업데이트 메소드 사용

        // 만약 비밀번호 업데이트 기능도 있다면 아래처럼 사용 (현재 UserUpdateRequest에는 없음)
         if (request.getUserPw() != null && !request.getUserPw().isEmpty()) {
            user.updatePassword(passwordEncoder.encode(request.getUserPw()));
        }

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

    // 프로필 수정 (닉네임, 연령대, 성별, 이미지)
    @Transactional
    public UserDto.UserResponse updateProfile(String userEmail, UserDto.ProfileUpdateRequest request) {
        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userEmail));

        // 닉네임 중복 확인 (다른 사용자가 이미 사용 중인지)
        if (request.getUserNm() != null && !request.getUserNm().equals(user.getUserNm())) {
            if (userRepository.existsByUserNm(request.getUserNm())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }

        // 문자열을 Enum으로 변환
        AgeGroup ageGroup = null;
        if (request.getAgeGroup() != null) {
            try {
                ageGroup = AgeGroup.fromDisplayName(request.getAgeGroup());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 연령대입니다: " + request.getAgeGroup());
            }
        }

        Gender gender = null;
        if (request.getGender() != null) {
            try {
                gender = Gender.fromDisplayName(request.getGender());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 성별입니다: " + request.getGender());
            }
        }

        user.updateProfile(request.getUserNm(), ageGroup, gender, request.getUserImg());
        return new UserDto.UserResponse(user);
    }

    // 중복 확인 메소드 추가
    @Transactional(readOnly = true)
    public boolean checkEmailExists(String email) {
        return userRepository.existsByUserEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean checkUserNmExists(String userName) {
        return userRepository.existsByUserNm(userName);
    }
}
