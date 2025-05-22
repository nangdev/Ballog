package notfound.ballog.domain.match.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.match.dto.MatchDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMatchListResponse {

    private List<MatchDto> matchList;

}
