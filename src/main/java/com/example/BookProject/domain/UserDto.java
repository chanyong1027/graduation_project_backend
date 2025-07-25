package com.example.BookProject.domain;

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
            this.userId = user.getUserId();
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

        public User toEntity() {
            // (보안) 나중에 Spring Security를 사용하면 여기서 비밀번호를 암호화해야 합니다.
            // BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            // this.userPw = encoder.encode(this.userPw);
            return new User(this.userEmail, this.userPw, this.userNm);
        }
    }

    /**
     * 회원 정보 수정을 위한 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserUpdateRequest {
        private String userNm; // 이름만 수정 가능하다고 가정
    }

}
