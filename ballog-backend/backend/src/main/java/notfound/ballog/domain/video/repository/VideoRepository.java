package notfound.ballog.domain.video.repository;

import notfound.ballog.domain.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Integer> {
    List<Video> findAllByMatch_MatchIdAndDeletedFalse(Integer matchId);

    Optional<Video> findByMatch_MatchIdAndQuarterNumberAndDeletedFalse(Integer matchId, Integer quarterNumber);

    Optional<Video> findByVideoUrl(String videoUrl);
}
