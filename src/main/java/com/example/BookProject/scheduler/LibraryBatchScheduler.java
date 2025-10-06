// com/example/BookProject/scheduler/LibraryBatchScheduler.java

package com.example.BookProject.scheduler;

import com.example.BookProject.service.LibraryBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryBatchScheduler {

    private final LibraryBatchService libraryBatchService;

    /**
     * 매일 새벽 4시에 도서관 데이터 업데이트 배치 작업을 실행합니다.
     * cron = "초 분 시 일 월 요일"
     * 0 0 4 * * ? = 매일 새벽 4시 0분 0초에 실행
     */
    //@Scheduled(cron = "0 0 4 * * ?")
    public void runLibraryDataBatch() {
        log.info("정기 도서관 데이터 업데이트 스케줄러를 실행합니다.");
        try {
            libraryBatchService.fetchAndSaveLibraries();
        } catch (Exception e) {
            log.error("도서관 데이터 배치 작업 중 오류가 발생했습니다.", e);
        }
    }
}