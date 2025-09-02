package com.example.BookProject.service;

import com.example.BookProject.dto.AladinDto;
import com.example.BookProject.domain.Book;
import com.example.BookProject.dto.BookDto;
import com.example.BookProject.repository.BookRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줌 (의존성 주입)
public class BookService {
    // final 키워드를 붙여서 의존성 주입 (생성자에서 초기화 보장)
    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;

    private final Map<String, LocalDateTime> searchCache = new ConcurrentHashMap<>();

    private static final String ALADIN_API_URL = "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx";
     // 사용자 인증키
     @Value("${external.api.aladin}")
     private String TTB_KEY;

    @Transactional
    public void searchAndSaveBooks(String query){
        // 1. URI 만들기
        // UriComponentsBuilder를 사용하면 파라미터를 안전하고 쉽게 추가할 수 있음

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        if (searchCache.containsKey(query) && searchCache.get(query).isAfter(oneHourAgo)) {
            log.info("캐시된 검색어입니다. API 호출을 생략합니다: {}", query);
            return;
        }

        log.info("알라딘 API를 호출합니다: {}", query);

        URI uri = UriComponentsBuilder.fromUriString(ALADIN_API_URL)
                .queryParam("ttbkey", TTB_KEY)
                .queryParam("Query", query)                     // 검색어
                .queryParam("QueryType", "Keyword")     // 검색어 종류
                .queryParam("MaxResults", 10)           // 최대 검색 결과 수
                .queryParam("start", 1)                 // 검색 시작 위치
                .queryParam("SearchTarget", "Book")     // 검색 대상
                .queryParam("output", "js")             // 출력 포맷 (JSON)
                .queryParam("Version", "20131101")
                .encode(StandardCharsets.UTF_8)// API 버전
                .build()                            // 한글 검색어가 깨지지 않도록 인코딩
                .toUri();

        // 2. RestTemplate으로 API 호출하기
        // getForObject 메소드는 GET 요청을 보내고, 응답받은 JSON을 우리가 만든 DTO 객체로 변환해줌
        AladinDto.AladinResponse response = restTemplate.getForObject(uri, AladinDto.AladinResponse.class);

        // ======================= 디버깅용 로그 추가 =======================
        // 실제로 알라딘에서 어떤 응답을 받았는지 확인하기 위해 추가합니다.
        log.info("알라딘 API 응답 수신. item 개수: {}", (response != null && response.getItem() != null) ? response.getItem().size() : "null 또는 0개");
        // ===============================================================

        // 3. API 호출이 성공하면, 캐시를 업데이트합니다.
        // 현재 검색어와 현재 시간을 캐시에 기록합니다.
        searchCache.put(query, LocalDateTime.now());


        // 3. 응답 결과 처리 및 DB 저장
        // API 응답이 null이 아니고, 책(item) 목록이 비어있지 않은 경우에만 처리
        if(response != null && response.getItem() != null && !response.getItem().isEmpty()){

            // 응답으로 받은 책 목록을 하나씩 순회
            for(AladinDto.Item item : response.getItem()){

                // isbn13 필드가 있는지 확인 (필수 값)
                if(item.getIsbn13() == null || item.getIsbn13().isEmpty()){
                    continue;
                }

                // ISBN을 기준으로 우리 DB에 이미 책이 저장되어 있는지 확인
                // findByIsbn의 결과가 비어있을 때 (즉, DB에 없을 때)만 저장 로직 실행
                if(bookRepository.findByIsbn(item.getIsbn13()).isEmpty()){

                    // DTO(item)에서 받은 정보로 Book 엔티티 생성
                    Book newBook = Book.builder()
                            .title(item.getTitle())
                            .author(item.getAuthor())
                            .publisher(item.getPublisher())
                            .isbn(item.getIsbn13())
                            .bookImg(item.getCover())
                            .publishedAt(item.getPubDate())
                            .description(item.getDescription())
                            .build();

                    // Repository를 통해 DB에 저장
                    bookRepository.save(newBook);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<BookDto.BookResponse> findAllBooks(){
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(BookDto.BookResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookDto.BookSearchResponse> searchBooks(String query){
        URI uri = UriComponentsBuilder.fromUriString(ALADIN_API_URL)
                .queryParam("ttbkey", TTB_KEY)
                .queryParam("Query", query)
                .queryParam("QueryType", "Keyword")
                .queryParam("MaxResults", 10)
                .queryParam("start", 1)
                .queryParam("SearchTarget", "Book")
                .queryParam("output", "js")
                .queryParam("Version", "20131101")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        AladinDto.AladinResponse response = restTemplate.getForObject(uri, AladinDto.AladinResponse.class);

        if(response != null && response.getItem() != null){
            return response.getItem().stream()
                    .map(BookDto.BookSearchResponse::new) // AladinDto.Item을 BookDto.BookSearchResponse로 변환
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Transactional
    public void saveBookByIsbn(String isbn){
        if(bookRepository.findByIsbn(isbn).isPresent()){
            log.info("이미 DB에 존재하는 책입니다. ISBN: {}", isbn);
            return;
        }

        URI uri = UriComponentsBuilder.fromUriString("http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx")
                .queryParam("ttbkey", TTB_KEY)
                .queryParam("itemId", isbn)
                .queryParam("itemIdType", "ISBN13")
                .queryParam("output", "js")
                .queryParam("Version", "20131101")
                .queryParam("Cover", "Big")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        AladinDto.AladinResponse response = restTemplate.getForObject(uri, AladinDto.AladinResponse.class);
        if(response != null && response.getItem() != null && !response.getItem().isEmpty()){
            AladinDto.Item item = response.getItem().get(0);
            Book newBook = Book.builder()
                    .title(item.getTitle())
                    .author(item.getAuthor())
                    .publisher(item.getPublisher())
                    .isbn(item.getIsbn13())
                    .bookImg(item.getCover())
                    .publishedAt(item.getPubDate())
                    .description(item.getDescription())
                    .build();
            bookRepository.save(newBook);
            log.info("새로운 책이 DB에 저장되었습니다: {}", item.getTitle());
        }
    }

    @PostConstruct
    @Transactional
    public void updateBestsellers() {
        log.info("베스트셀러 목록 업데이트를 시작합니다.");

        // 'ItemSearch.aspx' 대신 'ItemList.aspx'를 사용하고, QueryType을 'Bestseller'로 설정
        URI uri = UriComponentsBuilder.fromUriString("http://www.aladin.co.kr/ttb/api/ItemList.aspx")
                .queryParam("ttbkey", TTB_KEY)
                .queryParam("QueryType", "Bestseller") // <- 이 부분이 핵심
                .queryParam("MaxResults", 20)          // 베스트셀러 20개
                .queryParam("start", 1)
                .queryParam("SearchTarget", "Book")
                .queryParam("output", "js")
                .queryParam("Version", "20131101")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        AladinDto.AladinResponse response = restTemplate.getForObject(uri, AladinDto.AladinResponse.class);

        // 이하 로직은 searchAndSaveBooks와 동일 (DB에 없으면 저장)
        if (response != null && response.getItem() != null && !response.getItem().isEmpty()) {
            for (AladinDto.Item item : response.getItem()) {
                if (bookRepository.findByIsbn(item.getIsbn13()).isEmpty()) {
                    Book newBook = Book.builder()
                            .title(item.getTitle())
                            .author(item.getAuthor())
                            .publisher(item.getPublisher())
                            .isbn(item.getIsbn13())
                            .bookImg(item.getCover())
                            .build();
                    bookRepository.save(newBook);
                }
            }
        }
        log.info("베스트셀러 목록 업데이트를 완료했습니다.");
    }

}
