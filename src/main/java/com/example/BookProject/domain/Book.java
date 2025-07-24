package com.example.BookProject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "books")
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id", updatable = false)
    private Long bookId;

    @Column(name = "title", nullable=false)
    private String title;

    @Column(name = "author", nullable=false)
    private String author;

    @Column(name = "description")
    private String description;

    @Column(name = "book_img")
    private String bookImg;

    @Column(name = "isbn", unique=true)
    private String isbn;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "published_at")
    private String publishedAt;

    @Builder // 빌더 패턴으로 객체를 생성할 수 있게 해줌 (가독성 향상)
    public Book(String title, String author, String publisher, String isbn, String bookImg, String publishedAt, String description) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.bookImg = bookImg;
        this.publishedAt = publishedAt;
        this.description = description;
    }
}
