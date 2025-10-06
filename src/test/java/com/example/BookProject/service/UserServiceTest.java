package com.example.BookProject.service;

import com.example.BookProject.domain.User;
import com.example.BookProject.dto.UserDto;
import com.example.BookProject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


// ✅ @SpringBootTest: 실제 애플리케이션처럼 모든 Bean을 로드하여 통합 테스트 진행
// ✅ @Transactional: 각 테스트 후 DB 변경사항을 롤백(원상복구)하여 테스트 독립성 보장
@SpringBootTest
@Transactional
public class UserServiceTest {

    // ✅ @Autowired: Mock 객체 대신 실제 Bean을 주입받아 사용
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User existingUser;

    // 각 테스트 전에 미리 사용자 한 명을 저장해둡니다.
    @BeforeEach
    void setUp() {
        // 기존 데이터를 깔끔하게 지우고 시작
        userRepository.deleteAll();

        User userToSave = new User("test@example.com", passwordEncoder.encode("password123"), "기존유저");
        this.existingUser = userRepository.save(userToSave);
    }

    @DisplayName("새로운 사용자를 성공적으로 생성한다 (회원가입)")
    @Test
    void createUser_success() {
        // Given
        UserDto.UserCreateRequest request = new UserDto.UserCreateRequest();
        request.setUserEmail("new@example.com");
        request.setUserPw("newPassword");
        request.setUserNm("신규유저");

        // When
        UserDto.UserResponse response = userService.createUser(request);

        // Then
        assertThat(response).isNotNull();
        // ID가 DB에 의해 자동으로 생성되었는지 확인
        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getUserEmail()).isEqualTo("new@example.com");

        // 실제로 DB에 저장이 잘 되었는지, 비밀번호는 암호화되었는지 추가 검증
        User foundUser = userRepository.findByUserEmail("new@example.com").get();
        assertThat(foundUser.getUserNm()).isEqualTo("신규유저");
        assertThat(passwordEncoder.matches("newPassword", foundUser.getPassword())).isTrue();
    }

    @DisplayName("이미 존재하는 이메일로 회원가입 시 예외가 발생한다")
    @Test
    void createUser_duplicateEmail() {
        // Given
        UserDto.UserCreateRequest requestWithExistingEmail = new UserDto.UserCreateRequest();
        requestWithExistingEmail.setUserEmail("test@example.com"); // setUp에서 이미 저장한 이메일
        requestWithExistingEmail.setUserPw("anypassword");
        requestWithExistingEmail.setUserNm("아무개");

        // When & Then
        assertThatThrownBy(() -> userService.createUser(requestWithExistingEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @DisplayName("사용자 정보(이름)를 성공적으로 수정한다")
    @Test
    void updateUser_success() {
        // Given
        UserDto.UserUpdateRequest request = new UserDto.UserUpdateRequest();
        String updatedName = "수정된유저";
        request.setUserNm(updatedName);

        // When
        UserDto.UserResponse response = userService.updateUser(existingUser.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserNm()).isEqualTo(updatedName);
        assertThat(response.getUserEmail()).isEqualTo(existingUser.getUserEmail());

        // DB에서도 실제로 변경되었는지 확인
        User updatedUser = userRepository.findById(existingUser.getId()).get();
        assertThat(updatedUser.getUserNm()).isEqualTo(updatedName);
    }

    @DisplayName("존재하지 않는 사용자 ID로 업데이트 시 예외가 발생한다")
    @Test
    void updateUser_notFound() {
        // Given
        UserDto.UserUpdateRequest request = new UserDto.UserUpdateRequest();
        request.setUserNm("업데이트유저");
        Long nonExistentUserId = 999L; // 존재하지 않는 ID

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(nonExistentUserId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 ID의 사용자를 찾을 수 없습니다: " + nonExistentUserId);
    }

    @DisplayName("ID로 특정 사용자를 성공적으로 조회한다")
    @Test
    void findUserById_success() {
        // When
        UserDto.UserResponse response = userService.findUserById(existingUser.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(existingUser.getId());
        assertThat(response.getUserEmail()).isEqualTo(existingUser.getUserEmail());
        assertThat(response.getUserNm()).isEqualTo(existingUser.getUserNm());
    }

    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    @Test
    void findUserById_notFound() {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        assertThatThrownBy(() -> userService.findUserById(nonExistentUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 ID의 사용자를 찾을 수 없습니다: " + nonExistentUserId);
    }

    @DisplayName("이메일로 특정 사용자를 성공적으로 조회한다")
    @Test
    void findUserByEmail_success() {
        // When
        UserDto.UserResponse response = userService.findUserByEmail(existingUser.getUserEmail());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(existingUser.getId());
        assertThat(response.getUserEmail()).isEqualTo(existingUser.getUserEmail());
    }

    @DisplayName("존재하지 않는 이메일로 조회 시 예외가 발생한다")
    @Test
    void findUserByEmail_notFound() {
        // Given
        String nonExistentUserEmail = "none@example.com";

        // When & Then
        assertThatThrownBy(() -> userService.findUserByEmail(nonExistentUserEmail))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 ID의 사용자를 찾을 수 없습니다: " + nonExistentUserEmail);
    }

    @DisplayName("모든 사용자를 성공적으로 조회한다")
    @Test
    void findAllUsers_success() {
        // Given
        // 추가 사용자 생성
        User anotherUser = new User("another@example.com", passwordEncoder.encode("password456"), "다른유저");
        userRepository.save(anotherUser);

        // When
        List<UserDto.UserResponse> allUsers = userService.findAllUsers();

        // Then
        assertThat(allUsers).isNotNull();
        assertThat(allUsers.size()).isEqualTo(2); // setUp에서 만든 유저 + 방금 만든 유저
    }

    @DisplayName("사용자를 성공적으로 삭제한다")
    @Test
    void deleteUser_success() {
        // Given
        Long userIdToDelete = existingUser.getId();
        assertThat(userRepository.existsById(userIdToDelete)).isTrue(); // 삭제 전 존재 확인

        // When
        userService.deleteUser(userIdToDelete);

        // Then
        Optional<User> deletedUser = userRepository.findById(userIdToDelete);
        assertThat(deletedUser).isEmpty(); // 삭제 후 존재하지 않음을 확인
        assertThat(userRepository.existsById(userIdToDelete)).isFalse();
    }

    @DisplayName("존재하지 않는 사용자 ID로 삭제 시 예외가 발생한다")
    @Test
    void deleteUser_notFound() {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(nonExistentUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 ID의 사용자를 찾을 수 없습니다: " + nonExistentUserId);
    }

}