package notfound.ballog.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerCard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_card_seq_generator")
    @SequenceGenerator(
            name = "player_card_seq_generator",
            sequenceName = "player_card_seq",
            allocationSize = 1
    )
    private Integer cardId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "SMALLINT")
    private int speed;

    @Column(columnDefinition = "SMALLINT")
    private int stamina;

    @Column(columnDefinition = "SMALLINT")
    private int attack;

    @Column(columnDefinition = "SMALLINT")
    private int defense;

    @Column(columnDefinition = "SMALLINT")
    private int recovery;

    public static PlayerCard addBaseCard(User savedUser) {
        return PlayerCard.builder()
                .user(savedUser)
                .speed(0)
                .stamina(0)
                .attack(0)
                .defense(0)
                .recovery(0)
                .build();
    }
}
