package notfound.ballog.domain.match.entity;

import jakarta.persistence.*;
import lombok.*;
import notfound.ballog.domain.match.request.PersonalMatchAddRequest;
import notfound.ballog.domain.match.request.TeamMatchAddRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @SequenceGenerator(
            name = "match_sequence",
            sequenceName = "match_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "match_sequence"
    )
    private Integer matchId;

    private Integer teamId;

    @Column(nullable = false)
    private String matchName;

    @Column(nullable = false)

    //상태 ('예정', '진행중', '종료')
    private String matchStatus;

    @Column(nullable = false)
    private LocalDate matchDate;

    @Column(nullable = false, columnDefinition = "TIME")
    private LocalTime startTime;

    @Column(nullable = false, columnDefinition = "TIME")
    private LocalTime endTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Match(PersonalMatchAddRequest req) {
        this.matchName = req.getMatchName();
        this.matchStatus = "예정";
        this.matchDate = req.getMatchDate();
        this.startTime = req.getStartTime();
        this.endTime = req.getEndTime();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Match(TeamMatchAddRequest req) {
        this.teamId = req.getTeamId();
        this.matchName = req.getMatchName();
        this.matchStatus = "예정";
        this.matchDate = req.getMatchDate();
        this.startTime = req.getStartTime();
        this.endTime = req.getEndTime();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}
