package com.example.BookProject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilService {

    private final RestTemplate restTemplate;

    @Value("${external.api.kakao}")
    private String KAKAO_API_KEY;

    private static final String KAKAO_API_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

    public String getRegionFromCoords(double longitude, double latitude) {
        log.info("카카오 API를 통해 좌표를 주소로 변환합니다. lon={}, lat={}", longitude, latitude);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        URI uri = UriComponentsBuilder.fromUriString(KAKAO_API_URL)
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .build()
                .toUri();

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 API 호출 중 오류 발생", e);
            // 프론트엔드로 에러를 전달하기 위해 null 또는 예외를 던질 수 있습니다.
            // 여기서는 간단히 빈 JSON 객체를 반환합니다.
            return "{}";
        }
    }
}
