package notfound.ballog.domain.video.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "like_seq_generator")
    @SequenceGenerator(
            name = "like_seq_generator",
            sequenceName = "like_seq",
            allocationSize = 1
    )
    @Column(name = "like_id", nullable = false)
    private Integer likeId;

    @Column(name = "highlight_id", nullable = false)
    private Integer highlightId;

    @Column(name = "liked_user_id", nullable = false, columnDefinition = "UUID")
    private UUID likedUserId;

    @Column(name = "is_liked", nullable = false)
    private Boolean isLiked;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void toggleLikeStatus() {
        this.isLiked = !this.isLiked;
    }
}
