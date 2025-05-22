package notfound.ballog.domain.quarter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameReport {

    @Id
    @SequenceGenerator(
            name = "game_report_sequence",
            sequenceName = "game_report_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "game_report_sequence"
    )
    private Integer reportId;

    private UUID userId;

    private Integer quarterId;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> reportData;

    private String matchSide;

    private LocalDateTime createdAt;

    public GameReport(UUID userId, Integer quarterId, Map<String, Object> reportData, String matchSide) {
        this.userId = userId;
        this.quarterId = quarterId;
        this.reportData = reportData;
        this.matchSide = matchSide;
        this.createdAt = LocalDateTime.now();
    }

}
