package com.example.BookProject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Column(name = "user_pw", nullable = false, length = 100)
    private String userPw;

    @Column(name = "user_nm", nullable = false)
    private String userNm;

    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group")
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "user_img")
    private String userImg;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 생성 메소드
    public static User createUser(String userEmail, String userPw, String userNm,
                                  Gender gender, AgeGroup ageGroup) {
        User user = new User();
        user.userEmail = userEmail;
        user.userPw = userPw;
        user.userNm = userNm;
        user.gender = gender;
        user.ageGroup = ageGroup;
        return user;
    }

    public void updatePassword(String newPw) {
        this.userPw = newPw;
    }

    public void updateName(String newNm) {
        this.userNm = newNm;
    }

    public void updateProfile(String userNm, AgeGroup ageGroup, Gender gender, String userImg) {
        if (userNm != null) {
            this.userNm = userNm;
        }
        if (ageGroup != null) {
            this.ageGroup = ageGroup;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (userImg != null) {
            this.userImg = userImg;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.userPw;
    }

    @Override
    public String getUsername() {
        return this.userEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
