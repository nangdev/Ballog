package notfound.ballog.domain.video.response;

import lombok.*;
import notfound.ballog.domain.video.dto.VideoDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetVideoListResponse {
    private Integer totalQuarters;

    private List<VideoDto> quarterList;

    public static GetVideoListResponse of(Integer totalQuarters, List<VideoDto> quarterList) {
        return GetVideoListResponse.builder()
                .totalQuarters(totalQuarters)
                .quarterList(quarterList)
                .build();
    }

    public static GetVideoListResponse emptyOf(Integer totalQuarters) {
        return GetVideoListResponse.builder()
                .totalQuarters(totalQuarters)
                .quarterList(null)
                .build();
    }
}
