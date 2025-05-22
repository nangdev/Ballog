package notfound.ballog.domain.match.repository;

import notfound.ballog.domain.match.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Integer> {

    void deleteAllByMatchId(Integer matchId);

}
