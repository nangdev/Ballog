package notfound.ballog.domain.match.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    @SequenceGenerator(
            name = "participant_sequence",
            sequenceName = "participant_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "participant_sequence"
    )
    private Integer participantId;

    @Column(nullable = false)
    private Integer matchId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    //'팀원', '용병', '개인'
    private String participantType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Participant(UUID userId, Integer matchId, String participantType) {
        this.matchId = matchId;
        this.userId = userId;
        this.participantType = participantType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}
