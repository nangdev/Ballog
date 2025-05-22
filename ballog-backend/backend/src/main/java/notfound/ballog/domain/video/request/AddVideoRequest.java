package notfound.ballog.domain.video.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddVideoRequest {
    @NotNull
    private Integer matchId;

    @NotNull(message = "몇 쿼터인지 등록해주세요.")
    private Integer quarterNumber;

    @NotNull(message = "영상 길이를 입력해주세요.")
    @Pattern(
            regexp = "^\\d{2}:\\d{2}:\\d{2}$",
            message = "영상 길이는 HH:mm:ss 형식이어야 합니다. 예) 00:05:30"
    )
    private String duration;

    @NotBlank(message = "영상 url을 입력하세요.")
    private String videoUrl;
}
