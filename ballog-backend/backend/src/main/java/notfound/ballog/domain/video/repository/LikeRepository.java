package notfound.ballog.domain.video.repository;

import notfound.ballog.domain.video.entity.Like;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;


public interface LikeRepository extends JpaRepository<Like, Long> {

    // 특정 사용자와 하이라이트 ID 목록에 해당하는 모든 좋아요 레코드 조회
    @Query("SELECT l FROM Like l WHERE l.likedUserId = :userId AND l.highlightId IN :highlightIds")
    List<Like> findAllByLikedUserIdAndHighlightIdIn(@Param("userId") UUID userId, @Param("highlightIds") List<Integer> highlightIds);

    // 커서 기반 페이징을 위한 메서드
    @Query("SELECT l FROM Like l WHERE l.likedUserId = :userId AND l.isLiked = true " +
            "AND (:cursorId IS NULL OR l.highlightId < :cursorId) ")
    Slice<Like> findLikedHighlightsWithCursor(
            @Param("userId") UUID userId,
            @Param("cursorId") Integer cursorId,
            Pageable pageable);}
