package notfound.ballog.domain.team.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.team.dto.TeamMemberDto;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberListResponse {

    private List<TeamMemberDto> teamMemberList;

}
