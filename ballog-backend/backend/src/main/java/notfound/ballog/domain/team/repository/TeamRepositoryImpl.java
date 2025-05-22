package notfound.ballog.domain.team.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import notfound.ballog.domain.match.dto.ParticipantDto;
import notfound.ballog.domain.team.dto.CardStat;
import notfound.ballog.domain.team.dto.PlayerCardDto;
import notfound.ballog.domain.team.dto.TeamMemberDto;
import notfound.ballog.domain.team.entity.Team;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static notfound.ballog.domain.match.entity.QMatch.match;
import static notfound.ballog.domain.match.entity.QParticipant.participant;
import static notfound.ballog.domain.team.entity.QTeam.team;
import static notfound.ballog.domain.team.entity.QTeamMember.teamMember;
import static notfound.ballog.domain.user.entity.QPlayerCard.playerCard;
import static notfound.ballog.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Team> findAllByUserId(UUID userId) {
        return queryFactory
                .selectFrom(team)
                .join(teamMember)
                .on(team.teamId.eq(teamMember.teamId).and(teamMember.userId.eq(userId)))
                .fetch();
    }

    @Override
    public List<TeamMemberDto> findAllByTeamId(Integer teamId) {
        return queryFactory
                .select(Projections.constructor(TeamMemberDto.class,
                        teamMember.teamMemberId,
                        user.nickname,
                        teamMember.role))
                .from(teamMember)
                .join(user).on(user.userId.eq(teamMember.userId).and(teamMember.teamId.eq(teamId)))
                .fetch();
    }

    @Override
    public List<PlayerCardDto> findPlayerCardByTeamId(Integer teamId) {
        return queryFactory
                .select(Projections.constructor(PlayerCardDto.class,
                        user.nickname,
                        teamMember.role,
                        user.profileImageUrl,
                        Projections.constructor(CardStat.class,
                                playerCard.speed,
                                playerCard.stamina,
                                playerCard.defense,
                                playerCard.attack,
                                playerCard.recovery)))
                .from(teamMember)
                .where(teamMember.teamId.eq(teamId))
                .join(user).on(teamMember.userId.eq(user.userId))
                .join(playerCard).on(playerCard.user.userId.eq(user.userId))
                .orderBy(
                        new CaseBuilder()
                                .when(teamMember.role.eq("MANAGER")).then(0)
                                .otherwise(1).asc(),
                        user.nickname.asc())
                .fetch();
    }

    @Override
    public List<ParticipantDto> findParticipantsByUserIdAndMatchId(UUID userId, Integer matchId) {
        return queryFactory
                .select(Projections.constructor(ParticipantDto.class,
                        user.nickname,
                        teamMember.role,
                        user.profileImageUrl))
                .from(participant)
                .join(user).on(user.userId.eq(participant.userId))
                .leftJoin(teamMember).on(
                        teamMember.userId.eq(participant.userId)
                        .and(teamMember.teamId.eq(
                                JPAExpressions.select(match.teamId)
                                    .from(match)
                                    .where(match.matchId.eq(participant.matchId))
                        ))
                )
                .where(participant.matchId.eq(matchId))
                .fetch();
    }

}
