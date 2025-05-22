package notfound.ballog.domain.quarter.repository;

import notfound.ballog.domain.quarter.entity.GameReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GameReportRepository extends JpaRepository<GameReport, Integer> {

    /** 유저 ID로 조회 후 가장 최근 데이터 중 최대 5개 데이터 조회 */
    List<GameReport> findTop5ByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndQuarterId(UUID userId, Integer quarterId);

}
