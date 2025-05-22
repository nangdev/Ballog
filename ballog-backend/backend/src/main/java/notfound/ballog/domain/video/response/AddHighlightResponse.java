package notfound.ballog.domain.video.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddHighlightResponse {
    private Integer highlightId;

    public static AddHighlightResponse of(Integer highlightId) {
        return AddHighlightResponse.builder()
                .highlightId(highlightId)
                .build();
    }
}
