package notfound.ballog.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDto {

    private Integer matchId;

    private String matchName;

    private LocalDate matchDate;

    private LocalTime startTime;

    private LocalTime endTime;

}
