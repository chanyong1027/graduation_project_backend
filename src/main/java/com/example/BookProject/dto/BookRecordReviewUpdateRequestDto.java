package com.example.BookProject.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BookRecord의 개인 리뷰/별점 수정을 위한 DTO
 * (공개 Review 엔티티와는 별개)
 */
@Getter
@Setter
@NoArgsConstructor
public class BookRecordReviewUpdateRequestDto {

    private String review; // 개인 메모/리뷰

    @Min(value = 0, message = "별점은 0점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5점 이하이어야 합니다.")
    private Integer rating; // 0~5점
}
