package com.example.BookProject.dto;

import com.example.BookProject.domain.Library;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LibraryBookStatusDto {
    // Library 정보
    private Long libId;
    private Long d4lLibCode;
    private String libName;
    private String address;
    private String tel;
    private String homepage;
    private double latitude;
    private double longitude;

    // 책 소장 및 대출 정보
    private boolean hasBook;
    @JsonProperty("isLoanAvailable")
    private boolean loanAvailable;

    public LibraryBookStatusDto(Library library, boolean hasBook, boolean loanAvailable) {
        this.libId = library.getId();
        this.d4lLibCode = library.getD4lLibCode();
        this.libName = library.getLibName();
        this.address = library.getAddress();
        this.tel = library.getTel();
        this.homepage = library.getHomepage();
        this.latitude = library.getLatitude();
        this.longitude = library.getLongitude();
        this.hasBook = hasBook;
        this.loanAvailable = loanAvailable;
    }
}
