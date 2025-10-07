package com.example.BookProject.repository;

import com.example.BookProject.domain.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long> {
    // 정보나루 API의 libCode로 DB에 저장된 도서관이 있는지 확인
    Optional<Library> findByD4lLibCode(Long d4lLibCode);

    @Query("SELECT l FROM Library l WHERE l.latitude BETWEEN :minLat AND :maxLat AND l.longitude BETWEEN :minLon AND :maxLon")
    List<Library> findByLocationBounds(@Param("minLat") double minLat, @Param("maxLat") double maxLat,
                                       @Param("minLon") double minLon, @Param("maxLon") double maxLon);
}
