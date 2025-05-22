package notfound.ballog.domain.team.repository;

import notfound.ballog.domain.team.entity.TeamCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamCardRepository extends JpaRepository<TeamCard, Integer> {

    void deleteByTeamId(Integer teamId);

}
