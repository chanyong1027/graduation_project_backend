package com.example.BookProject.domain;

/**
 * 사용자 성별을 나타내는 Enum
 * 프론트엔드의 드롭다운 선택값과 매핑됩니다.
 */
public enum Gender {
    MALE("남자"),
    FEMALE("여자");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 문자열로부터 Gender를 찾는 헬퍼 메서드
     * "남자", "여자" 문자열을 받아서 해당하는 Enum을 반환합니다.
     */
    public static Gender fromDisplayName(String displayName) {
        for (Gender gender : Gender.values()) {
            if (gender.displayName.equals(displayName)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 성별입니다: " + displayName);
    }
}
