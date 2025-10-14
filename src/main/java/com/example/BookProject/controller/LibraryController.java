package com.example.BookProject.controller;

import com.example.BookProject.dto.LibraryBookStatusDto;
import com.example.BookProject.dto.LibraryDto;
import com.example.BookProject.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController // 이 클래스는 REST API를 처리하는 컨트롤러임을 선언
@RequiredArgsConstructor
@RequestMapping("/api/libraries") // 이 컨트롤러의 모든 API는 /api/libraries 로 시작
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping("/search")
    public ResponseEntity<List<LibraryDto.Response>> searchNearbyLibraries(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "distance", defaultValue = "3") double distance) {

        List<LibraryDto.Response> libraries = libraryService.searchNearbyLibraries(latitude, longitude, distance);
        return ResponseEntity.ok(libraries);
    }

    /**
     * 특정 도서관의 도서 대출 가능 여부를 확인하는 API
     * 예시 URL: GET /api/libraries/141002/availability?isbn=9788937460451
     * @param d4lLibCode 도서관 정보나루 고유 코드 (d4l_lib_code)
     * @param isbn 확인할 도서의 ISBN13
     * @return 대출 가능 여부 정보 (JSON)
     */
    @GetMapping("/{d4lLibCode}/availability")
    public ResponseEntity<LibraryDto.AvailabilityResponse> checkBookAvailability(
            @PathVariable("d4lLibCode") Long d4lLibCode,
            @RequestParam("isbn") String isbn) {

        LibraryDto.AvailabilityResponse availability = libraryService.checkBookAvailability(d4lLibCode, isbn);
        return ResponseEntity.ok(availability);
    }

    @GetMapping("/all-from-api")
    public ResponseEntity<String> getAllLibrariesFromApi(@RequestParam("pageNo") int pageNo, @RequestParam("pageSize") int pageSize) {
        String result = libraryService.getAllLibrariesFromApi(pageNo, pageSize);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/book-holding")
    public ResponseEntity<String> getBookHoldingLibrariesFromApi(
            @RequestParam("isbn") String isbn,
            @RequestParam("region") String region,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("pageSize") int pageSize) {
        String result = libraryService.getBookHoldingLibrariesFromApi(isbn, region, pageNo, pageSize);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search-from-api")
    public ResponseEntity<Object> searchLibrariesFromApi(
            @RequestParam("region") String region,
            @RequestParam("dtl_region") String dtl_region,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("pageSize") int pageSize) throws com.fasterxml.jackson.core.JsonProcessingException {
        String result = libraryService.searchLibrariesFromApi(region, dtl_region, pageNo, pageSize);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Object json = mapper.readValue(result, Object.class);
        return ResponseEntity.ok(json);
    }

    @GetMapping("/book-status")
    public ResponseEntity<List<LibraryBookStatusDto>> getLibraryBookStatuses(
            @RequestParam("isbn") String isbn,
            @RequestParam("region") String region) {
        List<LibraryBookStatusDto> result = libraryService.getLibraryBookStatuses(isbn, region);
        return ResponseEntity.ok(result);
    }

    // --- '내 도서관' 관련 API (API 명세서 기반) ---
    // 참고: 실제 구현 시에는 @AuthenticationPrincipal 등을 통해 로그인된 사용자 정보를 가져와야 합니다.
    // 지금은 userId를 파라미터로 받는다고 가정하겠습니다.

    @PostMapping("/{libraryId}/my-library")
    public ResponseEntity<Void> addMyLibrary(@PathVariable Long libraryId, @AuthenticationPrincipal UserDetails userDetails) {
        libraryService.addFavoriteLibrary(userDetails.getUsername(), libraryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-library")
    public ResponseEntity<List<LibraryDto.Response>> getMyLibraries(@AuthenticationPrincipal UserDetails userDetails) {
        List<LibraryDto.Response> myLibraries = libraryService.getMyLibraries(userDetails.getUsername());
        return ResponseEntity.ok(myLibraries);
    }

    @DeleteMapping("/{libraryId}/my-library")
    public ResponseEntity<Void> removeMyLibrary(@PathVariable Long libraryId, @AuthenticationPrincipal UserDetails userDetails) {
        libraryService.removeFavoriteLibrary(userDetails.getUsername(), libraryId);
        return ResponseEntity.ok().build();
    }
}