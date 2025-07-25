package com.example.BookProject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Libraries")
public class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "library_id")
    private Long id;

    @Column(name = "lib_code", unique = true, nullable = false)
    private Long libCode;

    @Column(name = "lib_name", nullable = false)
    private String libName;

    @Column(name = "address")
    private String address;

    @Column(name = "tel")
    private String tel;

    @Column(name = "homepage")
    private String homepage;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Builder
    public Library(Long libCode, String libName, String address, String tel, String homepage, String latitude, String longitude) {
        this.libCode = libCode;
        this.libName = libName;
        this.address = address;
        this.tel = tel;
        this.homepage = homepage;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
