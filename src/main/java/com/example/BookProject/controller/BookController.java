package com.example.BookProject.controller;

import com.example.BookProject.dto.BookDto;
import com.example.BookProject.dto.LibraryBookDto;
import com.example.BookProject.service.BookSearchService;
import com.example.BookProject.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookSearchService bookSearchService;

    @PostMapping("/search")
    public ResponseEntity<Void> searchAndSaveBooks(@RequestParam String query){
        bookService.searchAndSaveBooks(query);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<BookDto.BookResponse>> getAllBooks(){

        List<BookDto.BookResponse> bookResponses = bookService.findAllBooks();

        return ResponseEntity.ok().body(bookResponses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookDto.BookSearchResponse>> searchBooks(@RequestParam String query){
        List<BookDto.BookSearchResponse> searchResults = bookService.searchBooks(query);
        return ResponseEntity.ok().body(searchResults);
    }

    @PostMapping("/{isbn}")
    public ResponseEntity<Void> saveBook(@PathVariable String isbn){
        bookService.saveBookByIsbn(isbn);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search/libraries")
    public ResponseEntity<List<LibraryBookDto.SearchResponse>> searchBookInLibraries(
            @RequestParam("title") String title,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "distance", defaultValue = "5") double distance) {

        List<LibraryBookDto.SearchResponse> result = bookSearchService.searchBookAndGetLibraries(title, latitude, longitude, distance);
        return ResponseEntity.ok(result);
    }
}
