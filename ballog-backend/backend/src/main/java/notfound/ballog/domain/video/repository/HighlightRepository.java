package notfound.ballog.domain.video.repository;

import notfound.ballog.domain.video.entity.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HighlightRepository extends JpaRepository<Highlight, Integer> {
    Optional<Highlight> findByVideo_VideoIdAndDeletedFalse(Integer videoId);

    List<Highlight> findAllByVideo_VideoIdAndDeletedFalse(Integer videoId);
}
