package com.example.BookProject.domain;

import com.example.BookProject.dto.LibraryDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Libraries")
public class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "library_id")
    private Long id;

    @Column(name = "d4l_lib_code", unique = true)
    private Long d4lLibCode;

    @Column(name = "lib_name", nullable = false)
    private String libName;

    @Column(name = "address")
    private String address;

    @Column(name = "tel")
    private String tel;

    @Column(name = "homepage")
    private String homepage;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Builder
    public Library(Long d4lLibCode, String libName, String address, String tel, String homepage, Double latitude, Double longitude) {
        this.d4lLibCode = d4lLibCode;
        this.libName = libName;
        this.address = address;
        this.tel = tel;
        this.homepage = homepage;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setD4lLibCode(Long d4lLibCode) {
        this.d4lLibCode = d4lLibCode;
    }

}
