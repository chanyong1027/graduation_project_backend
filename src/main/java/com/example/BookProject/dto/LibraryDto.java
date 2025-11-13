package com.example.BookProject.dto;

import com.example.BookProject.domain.Library;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class LibraryDto {

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long id; // 프론트엔드 호환성을 위해 d4lLibCode를 id로 반환
        private Long d4lLibCode;
        private String libName;
        private String address;
        private String tel;
        private String homepage;
        private Double latitude;
        private Double longitude;

        public Response(Library library) {
            this.id = library.getD4lLibCode(); // 프론트엔드가 d4lLibCode를 id로 사용하므로
            this.d4lLibCode = library.getD4lLibCode();
            this.libName = library.getLibName();
            this.address = library.getAddress();
            this.tel = library.getTel();
            this.homepage = library.getHomepage();
            this.latitude = library.getLatitude();
            this.longitude = library.getLongitude();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class AvailabilityResponse {
        private Long libCode;
        private String isbn13;
        private Boolean hasBook;
        private Boolean loanAvailable;

        public AvailabilityResponse(Long libCode, String isbn13, boolean hasBook, boolean loanAvailable) {
            this.libCode = libCode;
            this.isbn13 = isbn13;
            this.hasBook = hasBook;
            this.loanAvailable = loanAvailable;
        }

        // boolean 타입은 Lombok의 @Getter가 get...이 아닌 is...로 생성할 수 있어 명시적으로 추가
        public Boolean isHasBook() {
            return hasBook;
        }

        public Boolean isLoanAvailable() {
            return loanAvailable;
        }
    }

    //--- 아래부터는 외부 API의 JSON 응답을 Java 객체로 매핑하기 위한 클래스들 ---//

    /**
     * '정보공개 도서관 조회' API의 JSON 응답을 매핑하기 위한 DTO
     */

    @Getter
    @NoArgsConstructor
    public static class Data4LibResponse {
        private ResponseData response;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ResponseData {
            private List<Lib> libs;
        }

        @Getter
        @NoArgsConstructor
        public static class Lib {
            private LibData lib;
        }

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class LibData {
            private Long libCode;
            private String libName;
            private String address;
            private String tel;
            private String homepage;
            private String latitude;
            private String longitude;
        }
    }
    /**
     * '도서 소장여부 및 대출 가능여부 조회' API의 JSON 응답을 매핑하기 위한 DTO
     */
    @Getter
    public static class Data4LibBookExistResponse {
        private BookExistResult response;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class BookExistResult {
            private ResultData result;
        }

        @Getter
        public static class ResultData {
            private String hasBook;
            private String loanAvailable;
        }
    }
}
