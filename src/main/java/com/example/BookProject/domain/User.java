package com.example.BookProject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity // 이 클래스가 데이터베이스 테이블과 매핑됨을 선언
@Getter // Lombok: 모든 필드의 Getter 메소드 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: 파라미터 없는 기본 생성자 자동 생성 (JPA는 기본 생성자가 필요)
@Table(name = "users") // 데이터베이스에 생성될 테이블 이름 지정 (지정 안하면 클래스 이름 따라감)
public class User {

    @Id // 기본 키(Primary Key)임을 선언
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 값을 DB가 자동으로 생성 (PostgreSQL의 bigserial과 잘 맞음)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Column(name = "user_pw", nullable = false)
    private String userPw;

    @Column(name = "user_nm", nullable = false)
    private String userNm;

    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail;

    // ERD에 있는 나머지 컬럼들도 동일한 방식으로 추가합니다.
    // USER_AGE, USER_GENDER, USER_IMG 등...
    @Column(name = "user_age")
    private Integer userAge;

    @Column(name = "user_gender")
    private String userGender;

    @Column(name = "user_img")
    private String userImg;

    @CreationTimestamp // 엔티티가 처음 생성될 때 시간 자동 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티가 수정될 때마다 시간 자동 저장
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //public User toEntity()를 위한 매개변수 존재 생성자 생성
    public User(String userEmail, String userPw, String userNm) {
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userNm = userNm;
    }

    // (참고) Setter를 무분별하게 열어두기보다, 명확한 의도를 가진 메소드를 만드는 것이 좋습니다.
    // 예: public void updateUser(String name, String image) { ... }
    public void updatePassword(String newPw) {
        this.userPw = newPw;
    }

    public void updateName(String newNm) {
        this.userNm = newNm;
    }

    public void updateUserProfile(String name, Integer age, String gender, String imgUrl) {
        this.userNm = name;
        this.userAge = age;
        this.userGender = gender;
        this.userImg = imgUrl;
    }
}
