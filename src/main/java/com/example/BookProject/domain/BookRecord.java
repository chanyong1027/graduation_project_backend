package com.example.BookProject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "book_record")
public class BookRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id", updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "read_status", nullable = false)
    private ReadStatus readStatus;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT") // 긴 글을 위한 TEXT 타입
    private String review;

    private Integer rating; // 0~5점

    @CreationTimestamp // 엔티티가 처음 생성될 때 시간 자동 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티가 수정될 때마다 시간 자동 저장
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public BookRecord(ReadStatus readStatus, LocalDate startDate, LocalDate endDate, Book book, User user) {
        this.readStatus = readStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.book = book;
        this.user = user;
    }

    //== 비즈니스 로직 편의 메서드 ==//
    public void updateStatus(ReadStatus newStatus, LocalDate customStartDate, LocalDate customEndDate) {
        this.readStatus = newStatus;

        if (newStatus == ReadStatus.Reading) {
            // startDate: 제공된 날짜 사용, 없으면 오늘 날짜
            this.startDate = (customStartDate != null) ? customStartDate : LocalDate.now();
            this.endDate = null;
        } else if (newStatus == ReadStatus.Completed) {
            // startDate: 제공된 날짜 사용, 없으면 기존 값 유지 또는 오늘 날짜
            if (customStartDate != null) {
                this.startDate = customStartDate;
            } else if (this.startDate == null) {
                this.startDate = LocalDate.now();
            }
            // endDate: 제공된 날짜 사용, 없으면 오늘 날짜
            this.endDate = (customEndDate != null) ? customEndDate : LocalDate.now();
        } else if (newStatus == ReadStatus.Wish) {
            // Wish로 변경 시 날짜 초기화 (선택적 날짜 무시)
            this.startDate = null;
            this.endDate = null;
        }
    }

    // 하위 호환성을 위한 오버로드 메서드 (날짜 없이 호출 시 자동 날짜 설정)
    public void updateStatus(ReadStatus newStatus) {
        updateStatus(newStatus, null, null);
    }

    public void updateReviewAndRating(String review, Integer rating) {
        this.review = review;
        this.rating = rating;
    }
}
