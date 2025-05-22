package notfound.ballog.domain.match.repository;

import notfound.ballog.domain.match.entity.Match;
import notfound.ballog.domain.quarter.response.AddQuarterAndGameReportResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer>, MatchRepositoryCustom {

    @Query("select new notfound.ballog.domain.quarter.response.AddQuarterAndGameReportResponse(m.matchId, m.matchName) from Match m where m.matchId = :matchId")
    AddQuarterAndGameReportResponse findMatchByUserIdAndMatchId(Integer matchId);

}