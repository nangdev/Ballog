package notfound.ballog.domain.team.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamInfoUpdateRequest {

    @NotNull
    private Integer teamId;

    @NotBlank(message = "팀 이름을 입력해주세요")
    private String teamName;

    private LocalDate foundationDate;

    private String logoImage;

}
