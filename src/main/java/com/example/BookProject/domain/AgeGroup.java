package com.example.BookProject.domain;

/**
 * 사용자 연령대를 나타내는 Enum
 * 프론트엔드의 드롭다운 선택값과 매핑됩니다.
 */
public enum AgeGroup {
    TEENS("10대"),
    TWENTIES("20대"),
    THIRTIES("30대"),
    FORTIES("40대"),
    FIFTIES("50대"),
    SIXTIES_PLUS("60대 이상");

    private final String displayName;

    AgeGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 문자열로부터 AgeGroup을 찾는 헬퍼 메서드
     * "10대", "20대" 등의 문자열을 받아서 해당하는 Enum을 반환합니다.
     */
    public static AgeGroup fromDisplayName(String displayName) {
        for (AgeGroup ageGroup : AgeGroup.values()) {
            if (ageGroup.displayName.equals(displayName)) {
                return ageGroup;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 연령대입니다: " + displayName);
    }
}
