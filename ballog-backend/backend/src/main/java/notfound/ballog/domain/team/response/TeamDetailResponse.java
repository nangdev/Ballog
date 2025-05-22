package notfound.ballog.domain.team.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.team.dto.PlayerCardDto;
import notfound.ballog.domain.team.dto.TeamCardDto;
import notfound.ballog.domain.team.dto.TeamDto;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDetailResponse {

    private String teamName;

    private String logoImageUrl;

    private LocalDate foundationDate;

    private TeamCardDto averageCardStats;

    private List<PlayerCardDto> playerCards;

    public static TeamDetailResponse of(TeamDto teamDto, TeamCardDto cardDto, List<PlayerCardDto> playerCards) {
        return TeamDetailResponse.builder()
                .teamName(teamDto.getTeamName())
                .logoImageUrl(teamDto.getLogoImageUrl())
                .foundationDate(teamDto.getFoundationDate())
                .averageCardStats(cardDto)
                .playerCards(playerCards)
                .build();
    }

}
