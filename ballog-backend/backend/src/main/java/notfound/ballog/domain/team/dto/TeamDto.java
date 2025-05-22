package notfound.ballog.domain.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.team.entity.Team;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDto {

    private Integer teamId;

    private String teamName;

    private String logoImageUrl;

    private LocalDate foundationDate;

    public static TeamDto of(Team team) {
        return TeamDto.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .logoImageUrl(team.getLogoImageUrl())
                .foundationDate(team.getFoundationDate())
                .build();
    }

}
