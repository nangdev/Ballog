package notfound.ballog.domain.team.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamCard {

    @Id
    @SequenceGenerator(
            name = "team_card_sequence",
            sequenceName = "team_card_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "team_card_sequence"
    )
    private Integer teamCardId;

    @Column(nullable = false)
    private Integer teamId;

    @Column(columnDefinition = "SMALLINT")
    private int avgSpeed;

    @Column(columnDefinition = "SMALLINT")
    private int avgStamina;

    @Column(columnDefinition = "SMALLINT")
    private int avgAttack;

    @Column(columnDefinition = "SMALLINT")
    private int avgDefense;

    @Column(columnDefinition = "SMALLINT")
    private int avgRecovery;

    public static TeamCard of(Integer teamId){
        return TeamCard.builder()
                .teamId(teamId)
                .build();
    }

}
