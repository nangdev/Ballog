package notfound.ballog.domain.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiRecommendResponse {

    private String heatmapAnalysis;
    private String sprintAnalysis;
    private String speedAnalysis;
    private String staminaAnalysis;
    private RecommendedPlayer recommendedPlayer;
    private String conclusion;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedPlayer {
        private String name;
        private String position;
        private String style;
        private String reason;
        private String train;
        private String imageUrl;
    }

}
