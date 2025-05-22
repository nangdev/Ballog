package notfound.ballog.domain.team.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.team.dto.TeamDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserTeamListResponse {

    private List<TeamDto> teamList;

}
