# CheckBook 백엔드 진행 상황 요약

## 1. 구현 완료 항목
- **도메인 & 저장소**: 도서, 사용자, 도서관, 독서 기록, 리뷰 등 핵심 엔티티와 JPA 레포지토리 구성 완료 (`src/main/java/com/example/BookProject/domain`, `repository`).
- **인증·인가**: 회원가입/로그인, JWT 발급 및 Spring Security 필터 체인으로 보호된 엔드포인트 구축 (`controller/UserController.java`, `jwt`, `config/SecurityConfig.java`).
- **도서 검색 기능**: 알라딘 API 연동을 통한 검색/상세/베스트셀러/신간 조회와 검색어 캐싱 구현 (`service/BookService.java`).
- **도서관 기능**: 좌표 기반 근처 도서관 조회, 정보나루 API를 활용한 소장/대출 상태 확인, 내 도서관 등록/조회/삭제 API 구현 (`service/LibraryService.java`).
- **독서 기록 관리**: 내 서재 추가, 상태 변경, 별점·리뷰 저장/수정/삭제 API 제공 (`controller/BookRecordController.java`, `service/BookRecordService.java`).

## 2. 미완료·리스크
- **프로필 P0 요건 미충족**: 기본 위치/반경, 내 도서관 최대 3개 제한 및 정렬 우선순위, 로그아웃·탈퇴 API가 빠져 있음 (`domain/User.java`, `service/LibraryService.java`).
- **리뷰 기능 버그**: JWT 기반 `UserDetails`를 도메인 `User`로 잘못 캐스팅하고, 리뷰 조회 경로 중복 및 repository 호출 오류로 실사용 불가 상태 (`controller/ReviewController.java`, `service/ReviewService.java`, `dto/ReviewResponseDto.java`).
- **도서 상세 가용성 누락**: 책 상세 응답이 도서관 소장/대출 정보와 내 도서관 우선 노출을 포함하지 않아 핵심 가치 제안을 충족하지 못함 (`controller/BookController.java`, `service/BookSearchService.java`).
- **비밀정보 노출**: 카카오 지오코딩 API 키가 코드에 하드코딩되어 있어 보안 및 배포 리스크 존재 (`service/KakaoApiService.java:22`).
- **테스트 부재**: `src/test/java`가 비어 있어 외부 연동/도메인 로직의 회귀 검증 수단이 없음.

## 3. 권장 후속 작업
1. 사용자 프로필·내 도서관 정책(P0 필수)을 충족하도록 엔드포인트/도메인 확장 및 검증 로직 추가.
2. 리뷰 API 버그 수정 및 JWT 인증 객체 처리 방식 개선, 엔드포인트 경로 정리.
3. 도서 상세 응답에 주변 도서관 가용성과 내 도서관 우선 정렬을 통합하여 프론트 요구에 맞춘다.
4. 외부 API 키를 환경 변수·시크릿 스토어로 이전하고 노출된 키를 교체.
5. 핵심 서비스와 외부 연동을 대상으로 최소 단위·통합 테스트를 작성해 안정성을 확보한다.
