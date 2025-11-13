package com.example.BookProject.dto;

import com.example.BookProject.domain.AgeGroup;
import com.example.BookProject.domain.Gender;
import com.example.BookProject.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class UserDto {

    /**
     * 회원 정보 응답을 위한 DTO
     * - 비밀번호 같은 민감 정보를 제외하고 클라이언트에게 전달
     */
    @Getter
    @NoArgsConstructor
    public static class UserResponse {
        private Long userId;
        private String userNm;
        private String userEmail;
        private String ageGroup; // Enum을 문자열로 변환하여 전달 (예: "10대", "20대")
        private String gender;   // Enum을 문자열로 변환하여 전달 (예: "남자", "여자")
        private String userImg;
        private LocalDateTime createdAt;

        public UserResponse(User user) {
            this.userId = user.getId();
            this.userNm = user.getUserNm();
            this.userEmail = user.getUserEmail();
            this.ageGroup = user.getAgeGroup() != null ? user.getAgeGroup().getDisplayName() : null;
            this.gender = user.getGender() != null ? user.getGender().getDisplayName() : null;
            this.userImg = user.getUserImg();
            this.createdAt = user.getCreatedAt();
        }
    }

    /**
     * 회원 생성을 위한 요청 DTO
     */
    @Getter
    @Setter // @RequestBody로 JSON 데이터를 받으려면 Setter 또는 생성자가 필요
    @NoArgsConstructor
    public static class UserCreateRequest {
        private String userNm;
        private String userEmail;
        private String userPw;
        private String gender;
        private String ageGroup;
    }

    /**
     * 회원 정보 수정을 위한 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserUpdateRequest {
        private String userNm; // 이름만 수정 가능하다고 가정
        private String userPw;
    }

    /**
     * 로그인 요청을 위한 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class LoginRequest {
        private String userEmail;
        private String userPw;
    }

    /**
     * 로그인 응답을 위한 DTO (JWT 토큰 포함)
     */
    @Getter
    @NoArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String userNm;
        private String userEmail;

        public LoginResponse(String accessToken, String refreshToken, Long userId, String userNm, String userEmail) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userId = userId;
            this.userNm = userNm;
            this.userEmail = userEmail;
        }
    }

    /**
     * Token Refresh 요청을 위한 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class TokenRefreshRequest {
        private String refreshToken;
    }

    /**
     * Token Refresh 응답을 위한 DTO
     */
    @Getter
    @NoArgsConstructor
    public static class TokenRefreshResponse {
        private String accessToken;
        private String refreshToken;

        public TokenRefreshResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    /**
     * 프로필 수정을 위한 요청 DTO
     * 프론트엔드에서 "10대", "20대", "남자", "여자" 형태의 문자열로 전송됩니다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProfileUpdateRequest {
        private String userNm;
        private String ageGroup; // "10대", "20대" 등의 문자열
        private String gender;   // "남자", "여자" 문자열
        private String userImg;
    }

}
