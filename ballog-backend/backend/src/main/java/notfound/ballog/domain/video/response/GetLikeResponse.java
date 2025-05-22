package notfound.ballog.domain.video.response;

import lombok.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetLikeResponse {
    private List<LikedHighlightInfo> highlights;
    private boolean hasNext;
    private Integer nextCursorId;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikedHighlightInfo {
        private Integer matchId;
        private String matchName;
        private String matchDate;
        private String startTime;
        private String endTime;
        private String highlightName;
        private String highlightStartTime;
        private Integer quarterNumber;
    }
}