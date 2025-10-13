package com.example.BookProject.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class KolisNetScraperService {

    private static final String BASE_URL = "https://www.nl.go.kr";
    private static final String SEARCH_URL = BASE_URL + "/kolisnet/search/searchResultAllList.do";
    private static final String EDITION_LIST_URL = BASE_URL + "/kolisnet/search/searchResultEditonList.do";
    private static final String HOLDING_LIB_URL = BASE_URL + "/kolisnet/search/searchHoldingListAjax.do";

    // [추가] 모든 요청에 사용할 User-Agent를 상수로 정의
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

    public Map<String, Boolean> getHoldingLibraries(String title) {
        try {
            // 1단계: 초기 접속 및 쿠키 획득
            Connection.Response initialResponse = Jsoup.connect(SEARCH_URL).userAgent(USER_AGENT).execute();
            Map<String, String> cookies = initialResponse.cookies();


            Thread.sleep(300); // 서버 부하 감소

            String ufKey = getUfKeyFromSearch(title, cookies);
            if (ufKey == null) return new HashMap<>();

            String bookKey = getBookKeyFromEditions(ufKey, cookies);
            if (bookKey == null) return new HashMap<>();

            return scrapeHoldingLibraries(ufKey, bookKey, cookies);
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUfKeyFromSearch(String title, Map<String, String> cookies) throws IOException, InterruptedException {


        // (선택) 요청 간 약간의 지연을 줘서 봇 차단 리스크 완화
        /*try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
        }*/

        // 2) 실제 검색 요청 — 반드시 필요한 파라미터 포함
        Document searchPage = Jsoup.connect(SEARCH_URL)
                .userAgent(USER_AGENT)
                .cookies(cookies)                          // <-- 쿠키 사용
                .header("Referer", BASE_URL + "/kolisnet/search/searchResultAllList.do")
                .header("X-Requested-With", "XMLHttpRequest") // 경우에 따라 필요
                .timeout(10000)
                .data("keywordType1", "total")
                .data("keyword1", title)
                .data("bookFilter", "BKGM,YON,BKDM,NK,NP,OT")
                .data("tab", "ALL")
                .data("pageNum", "1")
                .get();

        // 디버깅 출력: 실제 받은 HTML 앞부분과 selector 결과 수
        String htmlSnippet = searchPage.outerHtml().length() > 2000
                ? searchPage.outerHtml().substring(0, 2000)
                : searchPage.outerHtml();
        System.out.println("=== searchPage HTML snippet ===");
        System.out.println(htmlSnippet);
        System.out.println("resultList elements count: " + searchPage.select("ul.resultList").size());
        System.out.println("title anchors with fnEdtionList: " +
                searchPage.select("ul.resultList p.title a[onclick*='fnEdtionList']").size());

        Element editionLink = searchPage.select("ul.resultList p.title a[onclick*='fnEdtionList']").first();
        if (editionLink == null) {
            System.out.println("'" + title + "'에 대한 검색 결과(1단계)가 없습니다.");
            return null;
        }

        return extractKeyFromJs(editionLink.attr("onclick"));
    }

    private String getBookKeyFromEditions(String ufKey, Map<String, String> cookies) throws IOException {
        System.out.println("=== getBookKeyFromEditions() 시작, ufKey=" + ufKey);

        Document editionPage = Jsoup.connect(EDITION_LIST_URL)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .header("Referer", SEARCH_URL)
                .data("ufKey", ufKey)
                .timeout(10000)
                .get();

        String htmlSnippet = editionPage.outerHtml().length() > 2000
                ? editionPage.outerHtml().substring(0, 2000)
                : editionPage.outerHtml();
        System.out.println("=== editionPage HTML snippet ===");
        System.out.println(htmlSnippet);

        Elements links = editionPage.select("ul.searchList a.moreSearchList");
        System.out.println("found '소장도서관' 링크 개수: " + links.size());

        Element holdingLibLink = links.first();
        if (holdingLibLink == null) {
            System.out.println("판본 목록(2단계)에서 '소장도서관' 링크를 찾을 수 없습니다.");
            return null;
        }

        String onclickAttr = holdingLibLink.attr("onclick");
        System.out.println("onclick 내용: " + onclickAttr);

        return extractKeyFromJs(onclickAttr);
    }
    private Map<String, Boolean> scrapeHoldingLibraries(String ufKey, String bookKey, Map<String, String> cookies) throws IOException {
        String dynamicRefererUrl = EDITION_LIST_URL + "?ufKey=" + ufKey;

        Document doc = Jsoup.connect(HOLDING_LIB_URL)
                .userAgent(USER_AGENT) // <-- 여기는 원래 있었음
                .cookies(cookies)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Origin", BASE_URL) // [최종 수정] Origin 헤더 추가
                .header("Referer", dynamicRefererUrl)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8") // [최종 수정] Content-Type 명시
                .data("tab", "ALL")
                .data("searchType", "simple")
                .data("bookKey", bookKey)
                .ignoreContentType(true)
                .timeout(10000)
                .post();

        Map<String, Boolean> resultMap = new HashMap<>();
        Elements rows = doc.select(".holding_list > tbody > tr");

        for (Element row : rows) {
            String libraryName = row.select("td").get(0).text();
            String loanStatusText = row.select("td").get(4).text();
            boolean isAvailable = loanStatusText.contains("대출가능");
            resultMap.put(libraryName, isAvailable);
        }
        return resultMap;
    }

    private String extractKeyFromJs(String jsString) {
        Pattern pattern = Pattern.compile("'([0-9]+)'");
        Matcher matcher = pattern.matcher(jsString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}