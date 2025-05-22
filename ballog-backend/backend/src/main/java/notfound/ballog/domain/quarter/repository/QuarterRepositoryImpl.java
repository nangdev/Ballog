package notfound.ballog.domain.quarter.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import notfound.ballog.domain.quarter.dto.GameReportDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static notfound.ballog.domain.quarter.entity.QGameReport.gameReport;
import static notfound.ballog.domain.quarter.entity.QQuarter.quarter;

@Repository
@RequiredArgsConstructor
public class QuarterRepositoryImpl implements QuarterRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GameReportDto> findGameReportByUserIdAndMatchId(UUID userId, Integer matchId) {
        return queryFactory.select(Projections.constructor(GameReportDto.class,
                        quarter.quarterNumber,
                        gameReport.reportData,
                        gameReport.matchSide))
                .from(gameReport)
                .join(quarter).on(gameReport.quarterId.eq(quarter.quarterId))
                .where(
                        gameReport.userId.eq(userId),
                        quarter.matchId.eq(matchId)
                )
                .orderBy(quarter.quarterNumber.asc())
                .fetch();
    }

}
