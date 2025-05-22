package notfound.ballog.domain.video.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddS3VideoUrlRequest {
    @NotBlank(message = "파일명을 입력해주세요.")
    private String fileName;
}
