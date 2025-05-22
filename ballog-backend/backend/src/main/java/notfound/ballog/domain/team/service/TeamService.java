package notfound.ballog.domain.team.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.domain.team.dto.PlayerCardDto;
import notfound.ballog.domain.team.dto.TeamCardDto;
import notfound.ballog.domain.team.dto.TeamDto;
import notfound.ballog.domain.team.entity.Team;
import notfound.ballog.domain.team.entity.TeamCard;
import notfound.ballog.domain.team.entity.TeamMember;
import notfound.ballog.domain.team.repository.TeamCardRepository;
import notfound.ballog.domain.team.repository.TeamMemberRepository;
import notfound.ballog.domain.team.repository.TeamRepository;
import notfound.ballog.domain.team.request.TeamAddRequest;
import notfound.ballog.domain.team.request.TeamInfoUpdateRequest;
import notfound.ballog.domain.team.request.TeamMemberAddRequest;
import notfound.ballog.domain.team.response.TeamDetailResponse;
import notfound.ballog.domain.team.response.TeamMemberListResponse;
import notfound.ballog.domain.team.response.UserTeamListResponse;
import notfound.ballog.domain.user.repository.PlayerCardRepository;
import notfound.ballog.exception.InternalServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamCardRepository teamCardRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PlayerCardRepository playerCardRepository;

    @Transactional
    public void addTeam(UUID userId, TeamAddRequest teamAddRequest) {
        Team team = Team.of(teamAddRequest);
        teamRepository.save(team);

        teamCardRepository.save(TeamCard.of(team.getTeamId()));

        teamMemberRepository.save(TeamMember.of(userId, new TeamMemberAddRequest(team.getTeamId(), "MANAGER")));
    }

    public UserTeamListResponse getUserTeamList(UUID userId){
        List<TeamDto> teamDtoList = teamRepository.findAllByUserId(userId).stream()
                .map(TeamDto::of)
                .toList();

        return new UserTeamListResponse(teamDtoList);
    }

    public TeamDetailResponse getTeamDetail(Integer teamId){
        Team team = teamRepository.findById(teamId).orElse(null);
        TeamCard teamCard = teamCardRepository.findById(teamId).orElse(null);

        if(team == null || teamCard == null){
            throw new InternalServerException(BaseResponseStatus.TEAM_NOT_FOUND);
        }

        List<PlayerCardDto> playerCardDtoList = teamRepository.findPlayerCardByTeamId(teamId);

        return TeamDetailResponse.of(TeamDto.of(team), TeamCardDto.of(teamCard), playerCardDtoList);
    }

    public TeamMemberListResponse getTeamMemberList(Integer teamId){
        return new TeamMemberListResponse(teamRepository.findAllByTeamId(teamId));
    }

    @Transactional
    public void addTeamMember(UUID userId, TeamMemberAddRequest req){
        teamMemberRepository.save(TeamMember.of(userId, req));
    }

    @Transactional
    public void updateTeamInfo(UUID userId, TeamInfoUpdateRequest req){
        checkTeamMemberRole(userId, req.getTeamId());

        Team team = teamRepository.findById(req.getTeamId())
                .orElseThrow(() -> new InternalServerException(BaseResponseStatus.DATABASE_ERROR));

        team.setTeamName(req.getTeamName());
        team.setFoundationDate(req.getFoundationDate());
        team.setLogoImageUrl(req.getLogoImage());
    }

    @Transactional
    public void deleteTeam(UUID userId, Integer teamId){
        checkTeamMemberRole(userId, teamId);

        Integer teamMemberCount = teamMemberRepository.countTeamMembersByTeamId(teamId);

        if(teamMemberCount != 1){
            throw new InternalServerException(BaseResponseStatus.TEAM_NOT_EMPTY);
        }

        try {
            teamMemberRepository.deleteAllByTeamId(teamId);

            teamCardRepository.deleteByTeamId(teamId);

            teamRepository.deleteById(teamId);
        }catch (Exception e){
            throw new InternalServerException(BaseResponseStatus.DATABASE_ERROR);
        }
    }


    @Transactional
    public void deleteTeamMember(UUID userId, Integer teamId, Integer teamMemberId){
        checkTeamMemberRole(userId, teamId);

        teamMemberRepository.deleteById(teamMemberId);
    }

    private void checkTeamMemberRole(UUID userId, Integer teamId){
        String role = teamMemberRepository.findByUserIdAndTeamId(userId, teamId);

        if(!role.equals("MANAGER")){
            throw new InternalServerException(BaseResponseStatus.TEAMMEMBER_NOT_AUTHORIZED);
        }
    }

    @Transactional
    public void leaveTeam(UUID userId, Integer teamId){
        teamMemberRepository.deleteByUserIdAndTeamId(userId, teamId);
    }

    @Transactional
    public void updateAllTeamCards() {
        List<Integer> allTeamIds = teamRepository.findAllTeamIds();
        
        log.info("총 {} 개의 팀 카드 업데이트 시작", allTeamIds.size());
        
        for (Integer teamId : allTeamIds) {
            try {
                updateTeamCard(teamId);
            } catch (Exception e) {
                log.error("팀 카드 업데이트 중 오류 발생 - 팀 ID: {}, 오류: {}", teamId, e.getMessage());
            }
        }
    }

    @Transactional
    public void updateTeamCard(Integer teamId) {
        PlayerCardRepository.TeamCardProjection projection = playerCardRepository.calculateTeamAverages(teamId);
        
        if (projection == null || projection.getMemberCount() == 0) {
            log.info("팀 ID {}에 속한 팀원이 없거나 선수 카드가 없습니다.", teamId);
            return;
        }

        TeamCard teamCard = teamCardRepository.findById(teamId)
                .orElseThrow(() -> new InternalServerException(BaseResponseStatus.TEAM_NOT_FOUND));
        
        teamCard.setAvgSpeed(projection.getAvgSpeed());
        teamCard.setAvgStamina(projection.getAvgStamina());
        teamCard.setAvgAttack(projection.getAvgAttack());
        teamCard.setAvgDefense(projection.getAvgDefense());
        teamCard.setAvgRecovery(projection.getAvgRecovery());
        
        log.info("팀 ID {}의 팀 카드 업데이트 완료. 팀원 수: {}", teamId, projection.getMemberCount());
    }

}
