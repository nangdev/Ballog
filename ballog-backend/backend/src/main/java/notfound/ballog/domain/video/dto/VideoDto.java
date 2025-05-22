package notfound.ballog.domain.video.dto;

import lombok.*;
import notfound.ballog.domain.video.entity.Video;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoDto {
    private Integer videoId;

    private Integer quarterNumber;

    private String videoUrl;

    private List<HighlightDto> highlightList;

    public static VideoDto of(Video video, List<HighlightDto> highlightList) {
        return VideoDto.builder()
                .videoId(video.getVideoId())
                .quarterNumber(video.getQuarterNumber())
                .videoUrl(video.getVideoUrl())
                .highlightList(highlightList)
                .build();
    }
}
