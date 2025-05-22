package notfound.ballog.domain.quarter.repository;

import notfound.ballog.domain.quarter.dto.GameReportDto;

import java.util.List;
import java.util.UUID;

public interface QuarterRepositoryCustom {

    List<GameReportDto> findGameReportByUserIdAndMatchId(UUID userId, Integer matchId);

}
