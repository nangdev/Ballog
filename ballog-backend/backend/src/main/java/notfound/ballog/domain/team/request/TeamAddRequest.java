package notfound.ballog.domain.team.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamAddRequest {

    @NotBlank(message = "팀 이름을 입력해주세요")
    private String teamName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate foundationDate;

    private String logoImageUrl;

}
