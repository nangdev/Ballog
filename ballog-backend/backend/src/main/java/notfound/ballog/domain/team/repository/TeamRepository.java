package notfound.ballog.domain.team.repository;

import notfound.ballog.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> , TeamRepositoryCustom{

    @Query("select t.teamId from Team t")
    List<Integer> findAllTeamIds();

}
