package com.example.BookProject.dto;

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
        private String userImg;
        private LocalDateTime createdAt;

        public UserResponse(User user) {
            this.userId = user.getId();
            this.userNm = user.getUserNm();
            this.userImg = user.getUserImg();
            this.userEmail = user.getUserEmail();
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

        public LoginResponse(String accessToken) {
            this.accessToken = accessToken;
        }
    }

}
