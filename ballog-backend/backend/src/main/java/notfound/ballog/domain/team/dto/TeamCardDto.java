package notfound.ballog.domain.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.team.entity.TeamCard;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamCardDto {

    private Integer memberCount;

    private Integer avgSpeed;

    private Integer avgStamina;

    private Integer avgAttack;

    private Integer avgDefense;

    private Integer avgRecovery;

    public static TeamCardDto of(TeamCard teamCard) {
        return TeamCardDto.builder()
                .avgSpeed(teamCard.getAvgSpeed())
                .avgStamina(teamCard.getAvgStamina())
                .avgAttack(teamCard.getAvgAttack())
                .avgDefense(teamCard.getAvgDefense())
                .avgRecovery(teamCard.getAvgRecovery())
                .build();
    }

}
