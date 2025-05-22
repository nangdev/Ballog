package notfound.ballog.domain.team.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberAddRequest {

    @NotNull
    private Integer teamId;

    // 'MANAGER', 'MEMBER' ,'GUEST'
    @NotBlank(message = "팀 역할을 입력해주세요")
    private String role;

}
