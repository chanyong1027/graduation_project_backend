// com/example/BookProject/dto/NlssLibraryDto.java

package com.example.BookProject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * '국가도서관 통계시스템' API의 응답(JSON)을 Java 객체로 변환하기 위한 DTO 클래스
 */
@Getter
@NoArgsConstructor
public class NlssLibraryDto {

    private Result result;

    @Getter
    @NoArgsConstructor
    public static class Result {
        private List<Lib> list;
    }

    @Getter
    @NoArgsConstructor
    public static class Lib {
        private String libCode;
        private String libName;

        // JSON의 "addr" 필드를 이 변수에 매핑
        @JsonProperty("addr")
        private String address;

        @JsonProperty("libUrl")
        private String homepage;

        @JsonProperty("phone")
        private String tel;

        // JSON의 "geoX" 필드를 이 변수에 매핑
        @JsonProperty("geoX")
        private Double longitude;

        // JSON의 "geoY" 필드를 이 변수에 매핑
        @JsonProperty("geoY")
        private Double latitude;
    }
}