package com.example.BookProject.service;

import com.example.BookProject.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiService {
    private final RestTemplate restTemplate;

    //@Value("${external.api.kakao}")
    //private String kakaoApiKey;
    private String kakaoApiKey = "9f4c127055954893ddf23824b3c725ac";

    private static final String GEOCODE_URL = "https://dapi.kakao.com/v2/local/search/address.json?query=";

    public KakaoApiResponseDto.Document getCoordinates(String address) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            KakaoApiResponseDto response = restTemplate.exchange(GEOCODE_URL + address, HttpMethod.GET, entity, KakaoApiResponseDto.class).getBody();

            if (response != null && !response.getDocuments().isEmpty()) {
                // 성공 시에만 딜레이를 줍니다.
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return response.getDocuments().get(0);
            }
        } catch (Exception e) {
            // [디버깅 강화] 에러 발생 시, 원인을 확실히 보기 위해 에러를 다시 던져서 프로그램을 중단시킴
            System.err.println("!!!!!!!!!!! KAKAO API ERROR !!!!!!!!!!!");
            System.err.println("요청 주소: " + address);
            e.printStackTrace(); // 에러 내용 강제 출력
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            throw new RuntimeException(e); // 프로그램을 멈추기 위해 예외를 다시 던짐
        }
        return null;
    }
}
