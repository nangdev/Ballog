package notfound.ballog.domain.user.repository;

import notfound.ballog.domain.team.dto.TeamCardDto;
import notfound.ballog.domain.user.entity.PlayerCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PlayerCardRepository extends JpaRepository<PlayerCard, Integer> {

    Optional<PlayerCard> findByUser_UserId(UUID userId);

    // 프로젝션 인터페이스 정의
    interface TeamCardProjection {
        Integer getMemberCount();
        Integer getAvgSpeed();
        Integer getAvgStamina();
        Integer getAvgAttack();
        Integer getAvgDefense();
        Integer getAvgRecovery();
    }

    @Query(value = """
    SELECT 
        COUNT(pc.card_id) as memberCount,
        CAST(COALESCE(AVG(pc.speed), 0) AS INTEGER) as avgSpeed, 
        CAST(COALESCE(AVG(pc.stamina), 0) AS INTEGER) as avgStamina, 
        CAST(COALESCE(AVG(pc.attack), 0) AS INTEGER) as avgAttack, 
        CAST(COALESCE(AVG(pc.defense), 0) AS INTEGER) as avgDefense, 
        CAST(COALESCE(AVG(pc.recovery), 0) AS INTEGER) as avgRecovery
    FROM 
        player_card pc
    JOIN 
        team_member tm ON pc.user_id = tm.user_id
    WHERE 
        tm.team_id = :teamId
    """, nativeQuery = true)
    TeamCardProjection calculateTeamAverages(Integer teamId);
    
}
