package notfound.ballog.domain.video.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLikeRequest {

    @NotNull(message = "하이라이트 ID 목록은 null일 수 없습니다")
    @NotEmpty(message = "하이라이트 ID 목록은 비어있을 수 없습니다")
    List<Integer> highlightIds;
}
