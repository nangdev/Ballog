package notfound.ballog.domain.team.entity;

import jakarta.persistence.*;
import lombok.*;
import notfound.ballog.domain.team.request.TeamMemberAddRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    @SequenceGenerator(
            name = "team_member_sequence",
            sequenceName = "team_member_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "team_member_sequence"
    )
    private Integer teamMemberId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Integer teamId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    public static TeamMember of(UUID userId, TeamMemberAddRequest req) {
        return TeamMember.builder()
                .userId(userId)
                .teamId(req.getTeamId())
                .role(req.getRole())
                .joinedAt(LocalDateTime.now())
                .build();
    }

}
