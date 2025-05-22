package notfound.ballog.domain.video.request;

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
public class AddHighlightRequest {
    @NotNull(message = "영상 아이디를 입력해주세요.")
    private Integer videoId;

    private String highlightName;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;
}
