package notfound.ballog.domain.team.entity;

import jakarta.persistence.*;
import lombok.*;
import notfound.ballog.domain.team.request.TeamAddRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @SequenceGenerator(
            name = "team_sequence",
            sequenceName = "team_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "team_sequence"
    )
    private Integer teamId;

    @Column(nullable = false)
    private String teamName;

    @Column(nullable = false)
    private LocalDate foundationDate;

    @Column(columnDefinition = "TEXT")
    private String logoImageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Team of(TeamAddRequest req){
        LocalDateTime now = LocalDateTime.now();

        return Team.builder()
                .teamName(req.getTeamName())
                .foundationDate(req.getFoundationDate())
                .logoImageUrl(req.getLogoImageUrl())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
