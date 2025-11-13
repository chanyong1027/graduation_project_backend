package com.example.BookProject.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class ReviewRequestDto {

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    private String content;

    private String reviewImg;

    @NotNull(message = "별점은 필수입니다.")
    @Min(value = 0, message = "별점은 0점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5점 이하이어야 합니다.")
    private Float rating;

    // ISBN으로 책을 식별 (프론트엔드에서 ISBN을 주로 사용)
    @NotBlank(message = "도서 ISBN은 필수입니다.")
    private String isbn;

    // 실제 프로덕션에서는 SecurityContextHolder를 통해 인증된 사용자 정보를 가져오므로
    // userId는 DTO에 포함하지 않는 것이 더 안전합니다.
}
