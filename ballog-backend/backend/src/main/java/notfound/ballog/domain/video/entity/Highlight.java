package notfound.ballog.domain.video.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.video.dto.HighlightDto;
import notfound.ballog.domain.video.request.UpdateHighlightRequest;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Highlight {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "video_seq_generator")
    @SequenceGenerator(
            name = "video_seq_generator",
            sequenceName = "video_seq",
            allocationSize = 1
    )
    private Integer highlightId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(nullable = false)
    private String highlightName;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(columnDefinition = "boolean default false")
    private boolean deleted;

    public static Highlight of(Video video, HighlightDto highlightDto) {
        return Highlight.builder()
                .video(video)
                .highlightName(highlightDto.getHighlightName())
                .startTime(highlightDto.getStartTime())
                .endTime(highlightDto.getEndTime())
                .build();
    }

    public static Highlight toEntity(Video video, String highlightName, LocalTime startTime, LocalTime endTime) {
        return Highlight.builder()
                .video(video)
                .highlightName(highlightName)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    public void update(UpdateHighlightRequest request) {
        this.highlightName = request.getHighlightName();
        this.startTime = request.getStartTime();
        this.endTime = request.getEndTime();
    }

    public void delete() {
        this.deleted = true;
    }
}
