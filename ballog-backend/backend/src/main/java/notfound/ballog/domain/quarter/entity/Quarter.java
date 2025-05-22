package notfound.ballog.domain.quarter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quarter {

    @Id
    @SequenceGenerator(
            name = "quarter_sequence",
            sequenceName = "quarter_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "quarter_sequence"
    )
    private Integer quarterId;

    private Integer matchId;

    private Integer quarterNumber;

    private LocalDateTime createdAt;

    public Quarter(Integer matchId, Integer quarterNumber) {
        this.matchId = matchId;
        this.quarterNumber = quarterNumber;
        this.createdAt = LocalDateTime.now();
    }

}
