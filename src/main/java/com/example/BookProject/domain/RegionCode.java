package com.example.BookProject.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * 정보나루 API의 지역 코드
 * API Manual 62페이지 참조
 */
@Getter
@RequiredArgsConstructor
public enum RegionCode {
    SEOUL("11", "서울"),
    BUSAN("21", "부산"),
    DAEGU("22", "대구"),
    INCHEON("23", "인천"),
    GWANGJU("24", "광주"),
    DAEJEON("25", "대전"),
    ULSAN("26", "울산"),
    SEJONG("29", "세종"),
    GYEONGGI("31", "경기"),
    GANGWON("32", "강원"),
    CHUNGBUK("33", "충북"),
    CHUNGNAM("34", "충남"),
    JEONBUK("35", "전북"),
    JEONNAM("36", "전남"),
    GYEONGBUK("37", "경북"),
    GYEONGNAM("38", "경남"),
    JEJU("39", "제주");

    private final String code;
    private final String name;

    /**
     * 코드 값으로 RegionCode 찾기
     */
    public static Optional<RegionCode> fromCode(String code) {
        return Arrays.stream(values())
                .filter(region -> region.code.equals(code))
                .findFirst();
    }

    /**
     * 지역명으로 RegionCode 찾기 (부분 매칭)
     */
    public static Optional<RegionCode> fromName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        String normalized = name.trim();

        return Arrays.stream(values())
                .filter(region -> normalized.contains(region.name) || region.name.contains(normalized))
                .findFirst();
    }

    /**
     * 행정구역 주소에서 지역코드 추출
     * 예: "서울특별시 강남구" -> SEOUL
     * 예: "경기도 수원시" -> GYEONGGI
     */
    public static Optional<RegionCode> fromAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        String normalized = address.trim();

        // 특별시/광역시/도 키워드 우선 매칭
        if (normalized.startsWith("서울")) return Optional.of(SEOUL);
        if (normalized.startsWith("부산")) return Optional.of(BUSAN);
        if (normalized.startsWith("대구")) return Optional.of(DAEGU);
        if (normalized.startsWith("인천")) return Optional.of(INCHEON);
        if (normalized.startsWith("광주")) return Optional.of(GWANGJU);
        if (normalized.startsWith("대전")) return Optional.of(DAEJEON);
        if (normalized.startsWith("울산")) return Optional.of(ULSAN);
        if (normalized.startsWith("세종")) return Optional.of(SEJONG);
        if (normalized.startsWith("경기")) return Optional.of(GYEONGGI);
        if (normalized.startsWith("강원")) return Optional.of(GANGWON);
        if (normalized.startsWith("충청북도") || normalized.startsWith("충북")) return Optional.of(CHUNGBUK);
        if (normalized.startsWith("충청남도") || normalized.startsWith("충남")) return Optional.of(CHUNGNAM);
        if (normalized.startsWith("전라북도") || normalized.startsWith("전북")) return Optional.of(JEONBUK);
        if (normalized.startsWith("전라남도") || normalized.startsWith("전남")) return Optional.of(JEONNAM);
        if (normalized.startsWith("경상북도") || normalized.startsWith("경북")) return Optional.of(GYEONGBUK);
        if (normalized.startsWith("경상남도") || normalized.startsWith("경남")) return Optional.of(GYEONGNAM);
        if (normalized.startsWith("제주")) return Optional.of(JEJU);

        return Optional.empty();
    }
}
