package notfound.ballog.domain.team.repository;

import notfound.ballog.domain.match.dto.ParticipantDto;
import notfound.ballog.domain.team.dto.PlayerCardDto;
import notfound.ballog.domain.team.dto.TeamMemberDto;
import notfound.ballog.domain.team.entity.Team;

import java.util.List;
import java.util.UUID;

public interface TeamRepositoryCustom {

    List<Team> findAllByUserId(UUID userId);

    List<TeamMemberDto> findAllByTeamId(Integer teamId);

    List<PlayerCardDto> findPlayerCardByTeamId(Integer teamId);

    List<ParticipantDto> findParticipantsByUserIdAndMatchId(UUID userId, Integer matchId);

}
