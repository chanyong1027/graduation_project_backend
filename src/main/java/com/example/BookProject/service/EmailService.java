package com.example.BookProject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 이메일별 인증코드 저장소 (이메일 -> 인증코드 정보)
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    // 인증코드 정보를 담는 내부 클래스
    private static class VerificationCode {
        String code;
        LocalDateTime expiryTime;

        VerificationCode(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    /**
     * 6자리 랜덤 인증코드 생성
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    /**
     * 이메일 인증코드 전송
     *
     * @param email 수신자 이메일
     * @return 생성된 인증코드 (테스트용 - 실제 프로덕션에서는 반환하지 않음)
     */
    public String sendVerificationEmail(String email) {
        try {
            // 1. 인증코드 생성
            String code = generateVerificationCode();

            // 2. 만료시간 설정 (5분)
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

            // 3. 저장소에 보관 (기존 코드가 있으면 덮어씀)
            verificationCodes.put(email, new VerificationCode(code, expiryTime));

            // 4. 이메일 메시지 작성
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[CheckBook] 회원가입 인증코드");
            message.setText(
                "CheckBook 회원가입을 환영합니다!\n\n" +
                "인증코드: " + code + "\n\n" +
                "이 코드는 5분간 유효합니다.\n" +
                "본인이 요청하지 않았다면 이 이메일을 무시하세요."
            );

            // 5. 이메일 발송
            mailSender.send(message);

            log.info("인증코드 이메일 발송 성공: {} (만료시간: {})", email, expiryTime);

            return code; // 개발/테스트용 (실제 프로덕션에서는 제거)

        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다. 다시 시도해주세요.");
        }
    }

    /**
     * 인증코드 검증
     *
     * @param email 이메일
     * @param code 사용자가 입력한 인증코드
     * @return 인증 성공 여부
     */
    public boolean verifyCode(String email, String code) {
        VerificationCode storedCode = verificationCodes.get(email);

        // 1. 저장된 인증코드가 없는 경우
        if (storedCode == null) {
            log.warn("인증코드 없음: {}", email);
            return false;
        }

        // 2. 인증코드 만료 확인
        if (storedCode.isExpired()) {
            log.warn("인증코드 만료: {}", email);
            verificationCodes.remove(email); // 만료된 코드 삭제
            return false;
        }

        // 3. 인증코드 일치 확인
        boolean isValid = storedCode.code.equals(code);

        if (isValid) {
            log.info("인증 성공: {}", email);
            verificationCodes.remove(email); // 사용된 코드 삭제
        } else {
            log.warn("인증코드 불일치: {}", email);
        }

        return isValid;
    }

    /**
     * 만료된 인증코드 정리 (선택적 - 스케줄러로 주기적으로 실행 가능)
     */
    public void cleanupExpiredCodes() {
        verificationCodes.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.debug("만료된 인증코드 정리 완료");
    }
}
