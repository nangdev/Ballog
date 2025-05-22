package notfound.ballog.domain.match.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import notfound.ballog.domain.match.dto.MatchDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static notfound.ballog.domain.match.entity.QMatch.match;
import static notfound.ballog.domain.match.entity.QParticipant.participant;

@Repository
@RequiredArgsConstructor
public class MatchRepositoryImpl implements MatchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MatchDto> findMatchesByUserIdAndMonth(UUID userId, LocalDate startOfMonth, LocalDate endOfMonth) {
        return queryFactory
                .select(Projections.constructor(MatchDto.class,
                    match.matchId,
                    match.matchName,
                    match.matchDate,
                    match.startTime,
                    match.endTime))
                .from(match)
                .join(participant).on(match.matchId.eq(participant.matchId).and(participant.userId.eq(userId)))
                .where(match.matchDate.between(startOfMonth, endOfMonth))
                .orderBy(match.matchDate.asc())
                .fetch();
    }

    @Override
    public List<MatchDto> findMatchesByTeamIdAndMonth(Integer teamId, LocalDate startOfMonth, LocalDate endOfMonth) {
        return queryFactory
                .select(Projections.constructor(MatchDto.class,
                        match.matchId,
                        match.matchName,
                        match.matchDate,
                        match.startTime,
                        match.endTime))
                .distinct()
                .from(match)
                .join(participant).on(match.matchId.eq(participant.matchId))
                .where(match.teamId.eq(teamId), match.matchDate.between(startOfMonth, endOfMonth))
                .orderBy(match.matchDate.asc())
                .fetch();
    }

//    //자동 등록 버전
//    @Override
//    public Integer findMatchIdByUserIdAndMatchDate(UUID userId, LocalDate matchDate) {
//        List<Integer> userMatchIds = queryFactory
//                .select(participant.matchId)
//                .from(participant)
//                .where(participant.userId.eq(userId))
//                .orderBy(participant.participantId.desc())
//                .limit(5)
//                .fetch();
//
//
//        return queryFactory
//                .select(match.matchId)
//                .from(match)
//                .where(
//                        match.matchDate.eq(matchDate),
//                        match.matchId.in(userMatchIds)
//                )
//                .fetchOne();
//    }

    @Override
    public List<MatchDto> findMatchesByUserIdAndMatchDates(UUID userId, List<LocalDate> matchDates) {
        return queryFactory
                .select(Projections.constructor(MatchDto.class,
                        match.matchId,
                        match.matchName,
                        match.matchDate,
                        match.startTime,
                        match.endTime))
                .from(match)
                .join(participant).on(participant.matchId.eq(match.matchId).and(participant.userId.eq(userId)))
                .where(match.matchDate.in(matchDates))
                .fetch();
    }

}
