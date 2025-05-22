package notfound.ballog.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.user.entity.PlayerCard;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerCardStatListDto {
    private Integer speed;

    private Integer stamina;

    private Integer attack;

    private Integer defense;

    private Integer recovery;

    public static PlayerCardStatListDto of(PlayerCard playerCard) {
        return PlayerCardStatListDto.builder()
                .speed(playerCard.getSpeed())
                .stamina(playerCard.getStamina())
                .attack(playerCard.getAttack())
                .defense(playerCard.getDefense())
                .recovery(playerCard.getRecovery())
                .build();
    }
}
