package com.example.BookProject.service;

import com.example.BookProject.domain.Library;
import com.example.BookProject.domain.User;
import com.example.BookProject.domain.UserLibrary;
import com.example.BookProject.dto.LibraryDto;
import com.example.BookProject.repository.LibraryRepository;
import com.example.BookProject.repository.UserLibraryRepository;
import com.example.BookProject.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final UserLibraryRepository userLibraryRepository;
    private final UserRepository userRepository; // User 조회용

    // TODO: application.yml 로 분리하여 관리하는 것을 권장합니다.
    private final String API_KEY = "ad79e8a862abd18051e4d11cc8cd80446c48d9273cd1f9b332292508d9422682";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 외부 API를 통해 도서관을 검색하고, 그 결과를 DB에 저장(upsert)
     */
    @Transactional
    public List<LibraryDto.Response> searchLibraries(String region, String dtl_region) {
        String url = "http://data4library.kr/api/libSrch?authKey=" + API_KEY +
                "&region=" + region + "&dtl_region=" + dtl_region + "&pageSize=50&format=json";

        LibraryDto.Data4LibResponse response = restTemplate.getForObject(url, LibraryDto.Data4LibResponse.class);

        if(response == null || response.getResponse() == null || response.getResponse().getLibs() == null) {
            return Collections.emptyList();
        }
        
        return response.getResponse().getLibs().stream()
                .map(lib -> {
                    LibraryDto.Data4LibResponse.LibData libData = lib.getLib();

                    Library library = libraryRepository.findByLibCode(libData.getLibCode())
                            .orElseGet(() -> {
                                Library newLib = Library.builder()
                                        .libCode(libData.getLibCode())
                                        .libName(libData.getLibName())
                                        .address(libData.getAddress())
                                        .tel(libData.getTel())
                                        .homepage(libData.getHomepage())
                                        .latitude(libData.getLatitude())
                                        .longitude(libData.getLongitude())
                                        .build();
                                return libraryRepository.save(newLib);
                    });
                    return new LibraryDto.Response(library);
                })
                .collect(Collectors.toList());
    }

    /**
     * [JSON 버전] 도서 소장 여부 및 대출 가능 여부 조회 (실시간)
     */
    @Transactional(readOnly = true)
    public LibraryDto.AvailabilityResponse checkBookAvailability(Long libCode, String isbn13) {
        String url = "http://data4library.kr/api/bookExist?authKey=" + API_KEY
                + "&libCode=" + libCode + "&isbn13=" + isbn13 + "&format=json";

        try{
            LibraryDto.Data4LibBookExistResponse response = restTemplate.getForObject(url, LibraryDto.Data4LibBookExistResponse.class);
            LibraryDto.Data4LibBookExistResponse.ResultData result = response.getResponse().getResult();

            boolean hasBook = "Y".equals(result.getHasBook());
            boolean loanAvailable = "Y".equals(result.getLoanAvailable());

            return new LibraryDto.AvailabilityResponse(libCode, isbn13, hasBook, loanAvailable);
        } catch(Exception e){
            // API 호출 실패
            e.printStackTrace();
            return new LibraryDto.AvailabilityResponse(libCode, isbn13, false, false);
        }
    }

    /**
     * 내 도서관으로 추가 (즐겨찾기)
     */
    @Transactional
    public void addFavoriteLibrary(Long userId, Long libraryId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(()-> new IllegalArgumentException("도서관을 찾을 수 없습니다. Lib id: " + libraryId));

        if(userLibraryRepository.existsByUserAndLibrary_Id(user, libraryId)){
            throw new IllegalStateException("이미 추가된 도서관입니다.");
        }

        UserLibrary userLibrary = UserLibrary.builder()
                .user(user)
                .library(library)
                .build();

        userLibraryRepository.save(userLibrary);
    }

    @Transactional(readOnly = true)
    public List<LibraryDto.Response> getMyLabraries(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));
        return UserLibraryRepository.findByUser(user).stream()
                .map(userLibrary -> new LibraryDto.Response(userLibrary.getLibrary()))
                .collect(Collectors.toList());
    }

    /**
     * 내 도서관 삭제
     */
    @Transactional
    public void removeFavoriteLibrary(Long userId, Long libraryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));
        UserLibrary userLibrary = userLibraryRepository.findByUserAndLibrary_Id(user, libraryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 도서관이 내 목록에 없습니다."));

        userLibraryRepository.delete(userLibrary);
}
