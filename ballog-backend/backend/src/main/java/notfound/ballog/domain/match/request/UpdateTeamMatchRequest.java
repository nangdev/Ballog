package notfound.ballog.domain.match.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamMatchRequest {

    @NotNull
    private Integer matchId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate matchDate;

    @JsonFormat(pattern = "HH:mm")
    @NotNull
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @NotNull
    private LocalTime endTime;

    @NotBlank(message = "매치 이름을 입력해주세요")
    private String matchName;

    @NotNull
    private List<Integer> participantList;

}
