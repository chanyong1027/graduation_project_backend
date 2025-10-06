package com.example.BookProject.service;

import com.example.BookProject.domain.Library;
import com.example.BookProject.domain.User;
import com.example.BookProject.domain.UserLibrary;
import com.example.BookProject.dto.LibraryDto;
import com.example.BookProject.repository.LibraryRepository;
import com.example.BookProject.repository.UserLibraryRepository;
import com.example.BookProject.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${external.api.data4library}")
    private String DATA4L_API_KEY;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * [수정!] 외부 API들을 통해 도서관을 검색하고, 그 결과를 통합하여 반환
     * 1. 정보나루 API를 통해 도서관을 검색하고 DB에 저장 (Upsert)
     * 2. (추가) 다른 공공 API를 통해 도서관을 검색하고, 정보나루에 없던 도서관만 DB에 추가
     * 3. 최종적으로 DB에서 해당 지역의 모든 도서관을 조회하여 반환
     */
//    @Transactional
//    public List<LibraryDto.Response> searchLibraries(String region, String dtl_region) {
//        String url = "http://data4library.kr/api/libSrch?authKey=" + API_KEY +
//                "&region=" + region + "&dtl_region=" + dtl_region + "&pageSize=50&format=json";
//
//        LibraryDto.Data4LibResponse response = restTemplate.getForObject(url, LibraryDto.Data4LibResponse.class);
//
//        if(response == null || response.getResponse() == null || response.getResponse().getLibs() == null) {
//            return Collections.emptyList();
//        }
//
//        return response.getResponse().getLibs().stream()
//                .map(lib -> {
//                    LibraryDto.Data4LibResponse.LibData libData = lib.getLib();
//
//                    Library library = libraryRepository.findByLibCode(libData.getLibCode())
//                            .orElseGet(() -> {
//                                Library newLib = Library.builder()
//                                        .libCode(libData.getLibCode())
//                                        .libName(libData.getLibName())
//                                        .address(libData.getAddress())
//                                        .tel(libData.getTel())
//                                        .homepage(libData.getHomepage())
//                                        .latitude(libData.getLatitude())
//                                        .longitude(libData.getLongitude())
//                                        .build();
//                                return libraryRepository.save(newLib);
//                    });
//                    return new LibraryDto.Response(library);
//                })
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * [JSON 버전] 도서 소장 여부 및 대출 가능 여부 조회 (실시간)
//     */
//    @Transactional(readOnly = true)
//    public LibraryDto.AvailabilityResponse checkBookAvailability(Long libCode, String isbn13) {
//        String url = "http://data4library.kr/api/bookExist?authKey=" + API_KEY
//                + "&libCode=" + libCode + "&isbn13=" + isbn13 + "&format=json";
//
//        try{
//            LibraryDto.Data4LibBookExistResponse response = restTemplate.getForObject(url, LibraryDto.Data4LibBookExistResponse.class);
//            LibraryDto.Data4LibBookExistResponse.ResultData result = response.getResponse().getResult();
//
//            boolean hasBook = "Y".equals(result.getHasBook());
//            boolean loanAvailable = "Y".equals(result.getLoanAvailable());
//
//            return new LibraryDto.AvailabilityResponse(libCode, isbn13, hasBook, loanAvailable);
//        } catch(Exception e){
//            // API 호출 실패
//            e.printStackTrace();
//            return new LibraryDto.AvailabilityResponse(libCode, isbn13, false, false);
//        }
//    }

    /**
     * [대대적 수정!]
     * 사용자의 현재 위치(위도, 경도)와 검색 반경(distanceKm)을 기준으로,
     * 우리 DB에 저장된 도서관 목록을 검색하여 반환합니다.
     * 더 이상 외부 API를 실시간으로 호출하지 않습니다.
     *
     * @param latitude   사용자 현재 위치의 위도
     * @param longitude  사용자 현재 위치의 경도
     * @param distanceKm 검색 반경 (km)
     * @return 근처 도서관 목록
     */
    @Transactional(readOnly = true)
    public List<LibraryDto.Response> searchNearbyLibraries(double latitude, double longitude, double distanceKm) {
        // 1. 검색할 위도, 경도의 최소/최대 범위 계산
        //    지구 둘레, 위도 1도당 거리 등을 이용한 근사치 계산법입니다.
        final double EARTH_RADIUS_KM = 6371.0;
        double latRadian = Math.toRadians(latitude);
        double latDegree = 1 / (EARTH_RADIUS_KM * Math.cos(latRadian) * Math.PI / 180);
        double lonDegree = 1 / (EARTH_RADIUS_KM * Math.PI / 180);

        double minLat = latitude - (distanceKm / latDegree);
        double maxLat = latitude + (distanceKm / latDegree);
        double minLon = longitude - (distanceKm / lonDegree);
        double maxLon = longitude + (distanceKm / lonDegree);

        // 2. Repository에 새로 만든 메서드를 호출하여 사각형 범위 내의 도서관을 1차로 필터링
        List<Library> librariesInBounds = libraryRepository.findByLocationBounds(minLat, maxLat, minLon, maxLon);

        // 3. (선택적: 정확도 향상) 1차 필터링된 결과에 대해 실제 거리를 계산하여 반경 내의 도서관만 최종 선택
        return librariesInBounds.stream()
                .filter(library -> calculateDistance(latitude, longitude, library.getLatitude(), library.getLongitude()) <= distanceKm)
                .map(LibraryDto.Response::new) // Library 객체를 LibraryDto.Response 객체로 변환
                .collect(Collectors.toList());
    }

    /**
     * 두 지점 간의 거리를 계산하는 Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * 도서 소장 여부 및 대출 가능 여부 조회 (실시간)
     * 이 기능은 '도서관 정보나루' API를 실시간 호출해야만 알 수 있으므로 그대로 유지합니다.
     * 단, d4l_lib_code가 있는 도서관에 대해서만 호출 가능합니다.
     */
    @Transactional(readOnly = true)
    public LibraryDto.AvailabilityResponse checkBookAvailability(Long d4lLibCode, String isbn13) {
        // d4l_lib_code가 없는 도서관에 대한 예외처리
        libraryRepository.findByD4lLibCode(d4lLibCode)
                .orElseThrow(() -> new IllegalArgumentException("대출 정보 조회 기능이 지원되지 않는 도서관입니다."));

        String url = "http://data4library.kr/api/bookExist?authKey=" + DATA4L_API_KEY
                + "&libCode=" + d4lLibCode + "&isbn13=" + isbn13 + "&format=json";

        try {
            // ... (이하 로직은 기존과 동일)
            LibraryDto.Data4LibBookExistResponse response = restTemplate.getForObject(url, LibraryDto.Data4LibBookExistResponse.class);
            // ...
            boolean hasBook = "Y".equals(response.getResponse().getResult().getHasBook());
            boolean loanAvailable = "Y".equals(response.getResponse().getResult().getLoanAvailable());
            return new LibraryDto.AvailabilityResponse(d4lLibCode, isbn13, hasBook, loanAvailable);
        } catch (Exception e) {
            e.printStackTrace();
            return new LibraryDto.AvailabilityResponse(d4lLibCode, isbn13, false, false);
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
        return userLibraryRepository.findByUser(user).stream()
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
}
