package notfound.ballog.domain.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardStat {

    private int speed;

    private int stamina;

    private int attack;

    private int defense;

    private int recovery;

}
