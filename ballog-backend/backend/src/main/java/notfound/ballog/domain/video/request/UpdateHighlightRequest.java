package notfound.ballog.domain.video.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateHighlightRequest {
    @NotNull(message = "하이라이트 아이디를 입력하세요.")
    private Integer highlightId;

    @NotNull(message = "하이라이트 이름을 입력하세요.")
    @NotBlank(message = "하이라이트 이름을 입력하세요.")
    private String highlightName;

    @NotNull(message = "하이라이트 시작 시간을 입력하세요.")
    private LocalTime startTime;

    @NotNull(message = "하이라이트 종료 시간을 입력하세요.")
    private LocalTime endTime;
}
