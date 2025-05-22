package notfound.ballog.domain.video.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.match.entity.Match;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "video_seq_generator")
    @SequenceGenerator(
            name = "video_seq_generator",
            sequenceName = "video_seq",
            allocationSize = 1
    )
    private Integer videoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    private Integer quarterNumber;

    @Column(columnDefinition = "TEXT")
    private String videoUrl;

    @Column(columnDefinition = "INTERVAL")
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    private Duration duration;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(columnDefinition = "boolean default false")
    private boolean deleted;

    public static Video of(Match match, Integer quarterNumber, String videoUrl, Duration duration) {
        return Video.builder()
                .match(match)
                .quarterNumber(quarterNumber)
                .videoUrl(videoUrl)
                .duration(duration)
                .build();
    }

    public void save(Match match, Integer quarterNumber, String videoUrl, Duration duration) {
        this.match = match;
        this.quarterNumber = quarterNumber;
        this.videoUrl = videoUrl;
        this.duration = duration;
    }

    public static Video ofVideoUrl(String videoUrl) {
        return Video.builder()
                .videoUrl(videoUrl)
                .build();
    }

    public void delete() {
        this.deleted = true;
    }
}
