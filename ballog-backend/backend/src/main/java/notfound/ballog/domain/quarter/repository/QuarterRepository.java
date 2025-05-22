package notfound.ballog.domain.quarter.repository;

import notfound.ballog.domain.quarter.entity.Quarter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuarterRepository extends JpaRepository<Quarter, Integer>, QuarterRepositoryCustom {

    List<Quarter> findAllByMatchId(Integer matchId);

    List<Quarter> findAllByMatchIdAndQuarterNumberIn(Integer matchId, Collection<Integer> quarterNumbers);

    Integer countByMatchId(Integer matchId);

}
