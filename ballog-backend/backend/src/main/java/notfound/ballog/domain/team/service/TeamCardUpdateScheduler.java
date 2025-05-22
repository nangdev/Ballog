package notfound.ballog.domain.team.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamCardUpdateScheduler {

    private final TeamService teamService;

    // 매주 일요일 새벽 4시에 실행
    // @Scheduled(cron = "0 0 4 ? * SUN")
    
    // 테스트 용 매 정각마다 실행
    @Scheduled(cron = "0 0 * * * *")
    public void updateTeamCards() {
        log.info("팀 카드 주간 업데이트 스케줄러 시작");
        teamService.updateAllTeamCards();
        log.info("팀 카드 주간 업데이트 스케줄러 완료");
    }

}
