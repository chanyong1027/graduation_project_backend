package com.example.BookProject.dto;

import com.example.BookProject.domain.Library;
import lombok.Builder;
import lombok.Getter;

public class LibraryBookDto {

    @Getter
    public static class SearchResponse {
        // DB에서 가져온 정보
        private String libName;
        private String address;
        private Double latitude;
        private Double longitude;
        private String homepage;

        // 크롤링으로 가져온 실시간 정보
        private boolean loanAvailable;

        @Builder
        public SearchResponse(Library library, boolean loanAvailable) {
            this.libName = library.getLibName();
            this.address = library.getAddress();
            this.latitude = library.getLatitude();
            this.longitude = library.getLongitude();
            this.homepage = library.getHomepage();
            this.loanAvailable = loanAvailable;
        }
    }
}