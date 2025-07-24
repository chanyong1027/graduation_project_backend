package com.example.BookProject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
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
    public void updateStatus(ReadStatus newStatus) {
        this.readStatus = newStatus;
        if (newStatus == ReadStatus.Reading) {
            this.startDate = LocalDate.now();
            this.endDate = null;
        } else if (newStatus == ReadStatus.Completed) {
            if (this.startDate == null) { // 읽기 시작도 안하고 완독하는 경우
                this.startDate = LocalDate.now();
            }
            this.endDate = LocalDate.now();
        }
    }
}
