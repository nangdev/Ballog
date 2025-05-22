package notfound.ballog.domain.match.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.match.dto.ParticipantDto;
import notfound.ballog.domain.quarter.dto.GameReportDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchDetailResponse {

    private List<ParticipantDto> participantList;

    private List<GameReportDto> quarterList;

}
