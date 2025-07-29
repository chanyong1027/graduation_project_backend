package com.example.BookProject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class AladinDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AladinResponse {
        private List<Item> item;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String title;      // 책 제목
        private String author;     // 저자
        private String publisher;  // 출판사
        private String isbn13;     // 13자리 ISBN (가장 중요한 고유 식별자)
        private String cover;// 책 표지 이미지 URL
        private String description;
        private String pubDate;
        // description, pubDate 등 필요한 필드가 있다면 여기에 추가
    }
}
