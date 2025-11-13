package com.example.BookProject.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 정보나루 API의 세부지역 코드
 * API Manual 63~70페이지 참조
 */
@Getter
@RequiredArgsConstructor
public enum DetailedRegionCode {
    // 서울특별시 (11)
    SEOUL_JONGNO("11010", RegionCode.SEOUL, "종로구"),
    SEOUL_JUNG("11020", RegionCode.SEOUL, "중구"),
    SEOUL_YONGSAN("11030", RegionCode.SEOUL, "용산구"),
    SEOUL_SEONGDONG("11040", RegionCode.SEOUL, "성동구"),
    SEOUL_GWANGJIN("11050", RegionCode.SEOUL, "광진구"),
    SEOUL_DONGDAEMUN("11060", RegionCode.SEOUL, "동대문구"),
    SEOUL_JUNGNANG("11070", RegionCode.SEOUL, "중랑구"),
    SEOUL_SEONGBUK("11080", RegionCode.SEOUL, "성북구"),
    SEOUL_GANGBUK("11090", RegionCode.SEOUL, "강북구"),
    SEOUL_DOBONG("11100", RegionCode.SEOUL, "도봉구"),
    SEOUL_NOWON("11110", RegionCode.SEOUL, "노원구"),
    SEOUL_EUNPYEONG("11120", RegionCode.SEOUL, "은평구"),
    SEOUL_SEODAEMUN("11130", RegionCode.SEOUL, "서대문구"),
    SEOUL_MAPO("11140", RegionCode.SEOUL, "마포구"),
    SEOUL_YANGCHEON("11150", RegionCode.SEOUL, "양천구"),
    SEOUL_GANGSEO("11160", RegionCode.SEOUL, "강서구"),
    SEOUL_GURO("11170", RegionCode.SEOUL, "구로구"),
    SEOUL_GEUMCHEON("11180", RegionCode.SEOUL, "금천구"),
    SEOUL_YEONGDEUNGPO("11190", RegionCode.SEOUL, "영등포구"),
    SEOUL_DONGJAK("11200", RegionCode.SEOUL, "동작구"),
    SEOUL_GWANAK("11210", RegionCode.SEOUL, "관악구"),
    SEOUL_SEOCHO("11220", RegionCode.SEOUL, "서초구"),
    SEOUL_GANGNAM("11230", RegionCode.SEOUL, "강남구"),
    SEOUL_SONGPA("11240", RegionCode.SEOUL, "송파구"),
    SEOUL_GANGDONG("11250", RegionCode.SEOUL, "강동구"),

    // 부산광역시 (21)
    BUSAN_JUNG("21010", RegionCode.BUSAN, "중구"),
    BUSAN_SEO("21020", RegionCode.BUSAN, "서구"),
    BUSAN_DONG("21030", RegionCode.BUSAN, "동구"),
    BUSAN_YEONGDO("21040", RegionCode.BUSAN, "영도구"),
    BUSAN_BUSANJIN("21050", RegionCode.BUSAN, "부산진구"),
    BUSAN_DONGNAE("21060", RegionCode.BUSAN, "동래구"),
    BUSAN_NAM("21070", RegionCode.BUSAN, "남구"),
    BUSAN_BUK("21080", RegionCode.BUSAN, "북구"),
    BUSAN_HAEUNDAE("21090", RegionCode.BUSAN, "해운대구"),
    BUSAN_SAHA("21100", RegionCode.BUSAN, "사하구"),
    BUSAN_GEUMJEONG("21110", RegionCode.BUSAN, "금정구"),
    BUSAN_GANGSEO("21120", RegionCode.BUSAN, "강서구"),
    BUSAN_YEONJE("21130", RegionCode.BUSAN, "연제구"),
    BUSAN_SUYEONG("21140", RegionCode.BUSAN, "수영구"),
    BUSAN_SASANG("21150", RegionCode.BUSAN, "사상구"),
    BUSAN_GIJANG("21310", RegionCode.BUSAN, "기장군"),

    // 대구광역시 (22)
    DAEGU_JUNG("22010", RegionCode.DAEGU, "중구"),
    DAEGU_DONG("22020", RegionCode.DAEGU, "동구"),
    DAEGU_SEO("22030", RegionCode.DAEGU, "서구"),
    DAEGU_NAM("22040", RegionCode.DAEGU, "남구"),
    DAEGU_BUK("22050", RegionCode.DAEGU, "북구"),
    DAEGU_SUSEONG("22060", RegionCode.DAEGU, "수성구"),
    DAEGU_DALSEO("22070", RegionCode.DAEGU, "달서구"),
    DAEGU_DALSEONG("22310", RegionCode.DAEGU, "달성군"),

    // 인천광역시 (23)
    INCHEON_JUNG("23010", RegionCode.INCHEON, "중구"),
    INCHEON_DONG("23020", RegionCode.INCHEON, "동구"),
    INCHEON_NAM("23030", RegionCode.INCHEON, "남구"),
    INCHEON_YEONSU("23040", RegionCode.INCHEON, "연수구"),
    INCHEON_NAMDONG("23050", RegionCode.INCHEON, "남동구"),
    INCHEON_BUPYEONG("23060", RegionCode.INCHEON, "부평구"),
    INCHEON_GYEYANG("23070", RegionCode.INCHEON, "계양구"),
    INCHEON_SEO("23080", RegionCode.INCHEON, "서구"),
    INCHEON_GANGHWA("23310", RegionCode.INCHEON, "강화군"),
    INCHEON_ONGJIN("23320", RegionCode.INCHEON, "옹진군"),

    // 광주광역시 (24)
    GWANGJU_DONG("24010", RegionCode.GWANGJU, "동구"),
    GWANGJU_SEO("24020", RegionCode.GWANGJU, "서구"),
    GWANGJU_NAM("24030", RegionCode.GWANGJU, "남구"),
    GWANGJU_BUK("24040", RegionCode.GWANGJU, "북구"),
    GWANGJU_GWANGSAN("24050", RegionCode.GWANGJU, "광산구"),

    // 대전광역시 (25)
    DAEJEON_DONG("25010", RegionCode.DAEJEON, "동구"),
    DAEJEON_JUNG("25020", RegionCode.DAEJEON, "중구"),
    DAEJEON_SEO("25030", RegionCode.DAEJEON, "서구"),
    DAEJEON_YUSEONG("25040", RegionCode.DAEJEON, "유성구"),
    DAEJEON_DAEDEOK("25050", RegionCode.DAEJEON, "대덕구"),

    // 울산광역시 (26)
    ULSAN_JUNG("26010", RegionCode.ULSAN, "중구"),
    ULSAN_NAM("26020", RegionCode.ULSAN, "남구"),
    ULSAN_DONG("26030", RegionCode.ULSAN, "동구"),
    ULSAN_BUK("26040", RegionCode.ULSAN, "북구"),
    ULSAN_ULJU("26310", RegionCode.ULSAN, "울주군"),

    // 세종특별자치시 (29)
    SEJONG_CITY("29010", RegionCode.SEJONG, "세종시"),

    // 경기도 주요 시군 (31)
    GYEONGGI_SUWON("31010", RegionCode.GYEONGGI, "수원시"),
    GYEONGGI_SEONGNAM("31020", RegionCode.GYEONGGI, "성남시"),
    GYEONGGI_UIJEONGBU("31030", RegionCode.GYEONGGI, "의정부시"),
    GYEONGGI_ANYANG("31040", RegionCode.GYEONGGI, "안양시"),
    GYEONGGI_BUCHEON("31050", RegionCode.GYEONGGI, "부천시"),
    GYEONGGI_GWANGMYEONG("31060", RegionCode.GYEONGGI, "광명시"),
    GYEONGGI_PYEONGTAEK("31070", RegionCode.GYEONGGI, "평택시"),
    GYEONGGI_DONGDUCHEON("31080", RegionCode.GYEONGGI, "동두천시"),
    GYEONGGI_ANSAN("31090", RegionCode.GYEONGGI, "안산시"),
    GYEONGGI_GOYANG("31100", RegionCode.GYEONGGI, "고양시"),
    GYEONGGI_GWACHEON("31110", RegionCode.GYEONGGI, "과천시"),
    GYEONGGI_GURI("31120", RegionCode.GYEONGGI, "구리시"),
    GYEONGGI_NAMYANGJU("31130", RegionCode.GYEONGGI, "남양주시"),
    GYEONGGI_OSAN("31140", RegionCode.GYEONGGI, "오산시"),
    GYEONGGI_SIHEUNG("31150", RegionCode.GYEONGGI, "시흥시"),
    GYEONGGI_GUNPO("31160", RegionCode.GYEONGGI, "군포시"),
    GYEONGGI_UIWANG("31170", RegionCode.GYEONGGI, "의왕시"),
    GYEONGGI_HANAM("31180", RegionCode.GYEONGGI, "하남시"),
    GYEONGGI_YONGIN("31190", RegionCode.GYEONGGI, "용인시"),
    GYEONGGI_PAJU("31200", RegionCode.GYEONGGI, "파주시"),
    GYEONGGI_ICHEON("31210", RegionCode.GYEONGGI, "이천시"),
    GYEONGGI_ANSEONG("31220", RegionCode.GYEONGGI, "안성시"),
    GYEONGGI_GIMPO("31230", RegionCode.GYEONGGI, "김포시"),
    GYEONGGI_HWASEONG("31240", RegionCode.GYEONGGI, "화성시"),
    GYEONGGI_GWANGJU("31250", RegionCode.GYEONGGI, "광주시"),
    GYEONGGI_YANGJU("31260", RegionCode.GYEONGGI, "양주시"),
    GYEONGGI_POCHEON("31270", RegionCode.GYEONGGI, "포천시"),
    GYEONGGI_YEOJU("31280", RegionCode.GYEONGGI, "여주시"),
    GYEONGGI_YEONCHEON("31350", RegionCode.GYEONGGI, "연천군"),
    GYEONGGI_GAPYEONG("31370", RegionCode.GYEONGGI, "가평군"),
    GYEONGGI_YANGPYEONG("31380", RegionCode.GYEONGGI, "양평군"),

    // 강원특별자치도 (32)
    GANGWON_CHUNCHEON("32010", RegionCode.GANGWON, "춘천시"),
    GANGWON_WONJU("32020", RegionCode.GANGWON, "원주시"),
    GANGWON_GANGNEUNG("32030", RegionCode.GANGWON, "강릉시"),
    GANGWON_DONGHAE("32040", RegionCode.GANGWON, "동해시"),
    GANGWON_TAEBAEK("32050", RegionCode.GANGWON, "태백시"),
    GANGWON_SOKCHO("32060", RegionCode.GANGWON, "속초시"),
    GANGWON_SAMCHEOK("32070", RegionCode.GANGWON, "삼척시"),
    GANGWON_HONGCHEON("32310", RegionCode.GANGWON, "홍천군"),
    GANGWON_HOENGSEONG("32320", RegionCode.GANGWON, "횡성군"),
    GANGWON_YEONGWOL("32330", RegionCode.GANGWON, "영월군"),
    GANGWON_PYEONGCHANG("32340", RegionCode.GANGWON, "평창군"),
    GANGWON_JEONGSEON("32350", RegionCode.GANGWON, "정선군"),
    GANGWON_CHEORWON("32360", RegionCode.GANGWON, "철원군"),
    GANGWON_HWACHEON("32370", RegionCode.GANGWON, "화천군"),
    GANGWON_YANGGU("32380", RegionCode.GANGWON, "양구군"),
    GANGWON_INJE("32390", RegionCode.GANGWON, "인제군"),
    GANGWON_GOSEONG("32400", RegionCode.GANGWON, "고성군"),
    GANGWON_YANGYANG("32410", RegionCode.GANGWON, "양양군"),

    // 충청북도 (33)
    CHUNGBUK_CHUNGJU("33020", RegionCode.CHUNGBUK, "충주시"),
    CHUNGBUK_JECHEON("33030", RegionCode.CHUNGBUK, "제천시"),
    CHUNGBUK_CHEONGJU("33040", RegionCode.CHUNGBUK, "청주시"),
    CHUNGBUK_BOEUN("33320", RegionCode.CHUNGBUK, "보은군"),
    CHUNGBUK_OKCHEON("33330", RegionCode.CHUNGBUK, "옥천군"),
    CHUNGBUK_YEONGDONG("33340", RegionCode.CHUNGBUK, "영동군"),
    CHUNGBUK_JINCHEON("33350", RegionCode.CHUNGBUK, "진천군"),
    CHUNGBUK_GOESAN("33360", RegionCode.CHUNGBUK, "괴산군"),
    CHUNGBUK_EUMSEONG("33370", RegionCode.CHUNGBUK, "음성군"),
    CHUNGBUK_DANYANG("33380", RegionCode.CHUNGBUK, "단양군"),
    CHUNGBUK_JEUNGPYEONG("33390", RegionCode.CHUNGBUK, "증평군"),

    // 충청남도 (34)
    CHUNGNAM_CHEONAN("34010", RegionCode.CHUNGNAM, "천안시"),
    CHUNGNAM_GONGJU("34020", RegionCode.CHUNGNAM, "공주시"),
    CHUNGNAM_BORYEONG("34030", RegionCode.CHUNGNAM, "보령시"),
    CHUNGNAM_ASAN("34040", RegionCode.CHUNGNAM, "아산시"),
    CHUNGNAM_SEOSAN("34050", RegionCode.CHUNGNAM, "서산시"),
    CHUNGNAM_NONSAN("34060", RegionCode.CHUNGNAM, "논산시"),
    CHUNGNAM_GYERYONG("34070", RegionCode.CHUNGNAM, "계룡시"),
    CHUNGNAM_DANGJIN("34080", RegionCode.CHUNGNAM, "당진시"),
    CHUNGNAM_GEUMSAN("34310", RegionCode.CHUNGNAM, "금산군"),
    CHUNGNAM_BUYEO("34330", RegionCode.CHUNGNAM, "부여군"),
    CHUNGNAM_SEOCHEON("34340", RegionCode.CHUNGNAM, "서천군"),
    CHUNGNAM_CHEONGYANG("34350", RegionCode.CHUNGNAM, "청양군"),
    CHUNGNAM_HONGSEONG("34360", RegionCode.CHUNGNAM, "홍성군"),
    CHUNGNAM_YESAN("34370", RegionCode.CHUNGNAM, "예산군"),
    CHUNGNAM_TAEAN("34380", RegionCode.CHUNGNAM, "태안군"),

    // 전북특별자치도 (35)
    JEONBUK_JEONJU("35010", RegionCode.JEONBUK, "전주시"),
    JEONBUK_GUNSAN("35020", RegionCode.JEONBUK, "군산시"),
    JEONBUK_IKSAN("35030", RegionCode.JEONBUK, "익산시"),
    JEONBUK_JEONGEUP("35040", RegionCode.JEONBUK, "정읍시"),
    JEONBUK_NAMWON("35050", RegionCode.JEONBUK, "남원시"),
    JEONBUK_GIMJE("35060", RegionCode.JEONBUK, "김제시"),
    JEONBUK_WANJU("35310", RegionCode.JEONBUK, "완주군"),
    JEONBUK_JINAN("35320", RegionCode.JEONBUK, "진안군"),
    JEONBUK_MUJU("35330", RegionCode.JEONBUK, "무주군"),
    JEONBUK_JANGSU("35340", RegionCode.JEONBUK, "장수군"),
    JEONBUK_IMSIL("35350", RegionCode.JEONBUK, "임실군"),
    JEONBUK_SUNCHANG("35360", RegionCode.JEONBUK, "순창군"),
    JEONBUK_GOCHANG("35370", RegionCode.JEONBUK, "고창군"),
    JEONBUK_BUAN("35380", RegionCode.JEONBUK, "부안군"),

    // 전라남도 (36)
    JEONNAM_MOKPO("36010", RegionCode.JEONNAM, "목포시"),
    JEONNAM_YEOSU("36020", RegionCode.JEONNAM, "여수시"),
    JEONNAM_SUNCHEON("36030", RegionCode.JEONNAM, "순천시"),
    JEONNAM_NAJU("36040", RegionCode.JEONNAM, "나주시"),
    JEONNAM_GWANGYANG("36060", RegionCode.JEONNAM, "광양시"),
    JEONNAM_DAMYANG("36310", RegionCode.JEONNAM, "담양군"),
    JEONNAM_GOKSEONG("36320", RegionCode.JEONNAM, "곡성군"),
    JEONNAM_GURYE("36330", RegionCode.JEONNAM, "구례군"),
    JEONNAM_GOHEUNG("36350", RegionCode.JEONNAM, "고흥군"),
    JEONNAM_BOSEONG("36360", RegionCode.JEONNAM, "보성군"),
    JEONNAM_HWASUN("36370", RegionCode.JEONNAM, "화순군"),
    JEONNAM_JANGHEUNG("36380", RegionCode.JEONNAM, "장흥군"),
    JEONNAM_GANGJIN("36390", RegionCode.JEONNAM, "강진군"),
    JEONNAM_HAENAM("36400", RegionCode.JEONNAM, "해남군"),
    JEONNAM_YEONGAM("36410", RegionCode.JEONNAM, "영암군"),
    JEONNAM_MUAN("36420", RegionCode.JEONNAM, "무안군"),
    JEONNAM_HAMPYEONG("36430", RegionCode.JEONNAM, "함평군"),
    JEONNAM_YEONGGWANG("36440", RegionCode.JEONNAM, "영광군"),
    JEONNAM_JANGSEONG("36450", RegionCode.JEONNAM, "장성군"),
    JEONNAM_WANDO("36460", RegionCode.JEONNAM, "완도군"),
    JEONNAM_JINDO("36470", RegionCode.JEONNAM, "진도군"),
    JEONNAM_SINAN("36480", RegionCode.JEONNAM, "신안군"),

    // 경상북도 (37)
    GYEONGBUK_POHANG("37010", RegionCode.GYEONGBUK, "포항시"),
    GYEONGBUK_GYEONGJU("37020", RegionCode.GYEONGBUK, "경주시"),
    GYEONGBUK_GIMCHEON("37030", RegionCode.GYEONGBUK, "김천시"),
    GYEONGBUK_ANDONG("37040", RegionCode.GYEONGBUK, "안동시"),
    GYEONGBUK_GUMI("37050", RegionCode.GYEONGBUK, "구미시"),
    GYEONGBUK_YEONGJU("37060", RegionCode.GYEONGBUK, "영주시"),
    GYEONGBUK_YEONGCHEON("37070", RegionCode.GYEONGBUK, "영천시"),
    GYEONGBUK_SANGJU("37080", RegionCode.GYEONGBUK, "상주시"),
    GYEONGBUK_MUNGYEONG("37090", RegionCode.GYEONGBUK, "문경시"),
    GYEONGBUK_GYEONGSAN("37100", RegionCode.GYEONGBUK, "경산시"),
    GYEONGBUK_GUNWI("37310", RegionCode.GYEONGBUK, "군위군"),
    GYEONGBUK_UISEONG("37320", RegionCode.GYEONGBUK, "의성군"),
    GYEONGBUK_CHEONGSONG("37330", RegionCode.GYEONGBUK, "청송군"),
    GYEONGBUK_YEONGYANG("37340", RegionCode.GYEONGBUK, "영양군"),
    GYEONGBUK_YEONGDEOK("37350", RegionCode.GYEONGBUK, "영덕군"),
    GYEONGBUK_CHEONGDO("37360", RegionCode.GYEONGBUK, "청도군"),
    GYEONGBUK_GORYEONG("37370", RegionCode.GYEONGBUK, "고령군"),
    GYEONGBUK_SEONGJU("37380", RegionCode.GYEONGBUK, "성주군"),
    GYEONGBUK_CHILGOK("37390", RegionCode.GYEONGBUK, "칠곡군"),
    GYEONGBUK_YECHEON("37400", RegionCode.GYEONGBUK, "예천군"),
    GYEONGBUK_BONGHWA("37410", RegionCode.GYEONGBUK, "봉화군"),
    GYEONGBUK_ULJIN("37420", RegionCode.GYEONGBUK, "울진군"),
    GYEONGBUK_ULLEUNG("37430", RegionCode.GYEONGBUK, "울릉군"),

    // 경상남도 (38)
    GYEONGNAM_JINJU("38030", RegionCode.GYEONGNAM, "진주시"),
    GYEONGNAM_TONGYEONG("38050", RegionCode.GYEONGNAM, "통영시"),
    GYEONGNAM_SACHEON("38060", RegionCode.GYEONGNAM, "사천시"),
    GYEONGNAM_GIMHAE("38070", RegionCode.GYEONGNAM, "김해시"),
    GYEONGNAM_MILYANG("38080", RegionCode.GYEONGNAM, "밀양시"),
    GYEONGNAM_GEOJE("38090", RegionCode.GYEONGNAM, "거제시"),
    GYEONGNAM_YANGSAN("38100", RegionCode.GYEONGNAM, "양산시"),
    GYEONGNAM_CHANGWON("38110", RegionCode.GYEONGNAM, "창원시"),
    GYEONGNAM_UIRYEONG("38310", RegionCode.GYEONGNAM, "의령군"),
    GYEONGNAM_HAMAN("38320", RegionCode.GYEONGNAM, "함안군"),
    GYEONGNAM_CHANGNYEONG("38330", RegionCode.GYEONGNAM, "창녕군"),
    GYEONGNAM_GOSEONG("38340", RegionCode.GYEONGNAM, "고성군"),
    GYEONGNAM_NAMHAE("38350", RegionCode.GYEONGNAM, "남해군"),
    GYEONGNAM_HADONG("38360", RegionCode.GYEONGNAM, "하동군"),
    GYEONGNAM_SANCHEONG("38370", RegionCode.GYEONGNAM, "산청군"),
    GYEONGNAM_HAMYANG("38380", RegionCode.GYEONGNAM, "함양군"),
    GYEONGNAM_GEOCHANG("38390", RegionCode.GYEONGNAM, "거창군"),
    GYEONGNAM_HAPCHEON("38400", RegionCode.GYEONGNAM, "합천군"),

    // 제주특별자치도 (39)
    JEJU_JEJU("39010", RegionCode.JEJU, "제주시"),
    JEJU_SEOGWIPO("39020", RegionCode.JEJU, "서귀포시");

    private final String code;
    private final RegionCode regionCode;
    private final String name;

    /**
     * 코드 값으로 DetailedRegionCode 찾기
     */
    public static Optional<DetailedRegionCode> fromCode(String code) {
        return Arrays.stream(values())
                .filter(detail -> detail.code.equals(code))
                .findFirst();
    }

    /**
     * 특정 광역시도의 세부지역 목록 조회
     */
    public static List<DetailedRegionCode> getByRegion(RegionCode region) {
        return Arrays.stream(values())
                .filter(detail -> detail.regionCode == region)
                .collect(Collectors.toList());
    }

    /**
     * 세부지역명으로 DetailedRegionCode 찾기
     */
    public static Optional<DetailedRegionCode> fromName(RegionCode region, String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        String normalized = name.trim();

        return Arrays.stream(values())
                .filter(detail -> detail.regionCode == region)
                .filter(detail -> normalized.contains(detail.name) || detail.name.contains(normalized))
                .findFirst();
    }
}
