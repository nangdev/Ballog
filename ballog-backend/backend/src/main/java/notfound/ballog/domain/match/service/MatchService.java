package notfound.ballog.domain.match.service;

import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.domain.match.dto.MatchDto;
import notfound.ballog.domain.match.dto.ParticipantDto;
import notfound.ballog.domain.match.entity.Match;
import notfound.ballog.domain.match.entity.Participant;
import notfound.ballog.domain.match.repository.MatchRepository;
import notfound.ballog.domain.match.repository.ParticipantRepository;
import notfound.ballog.domain.match.request.*;
import notfound.ballog.domain.match.response.GetMatchListByDateResponse;
import notfound.ballog.domain.match.response.MatchDetailResponse;
import notfound.ballog.domain.quarter.dto.GameReportDto;
import notfound.ballog.domain.quarter.repository.QuarterRepository;
import notfound.ballog.domain.team.repository.TeamMemberRepository;
import notfound.ballog.domain.team.repository.TeamRepository;
import notfound.ballog.exception.InternalServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final QuarterRepository quarterRepository;

    @Transactional
    public void addPesonalMatch(UUID userId, PersonalMatchAddRequest req){
        Match match = new Match(req);

        matchRepository.save(match);

        participantRepository.save(new Participant(userId, match.getMatchId(), "개인"));
    }

    @Transactional
    public void addTeamMatch(TeamMatchAddRequest req){
        Match match = new Match(req);

        matchRepository.save(match);

        Integer matchId = match.getMatchId();

        List<UUID> userIds = teamMemberRepository.findUserIdsByTeamMemberIds(req.getParticipantList());

        List<Participant> participants = userIds.stream()
                .map(userId -> new Participant(userId, matchId, "팀원"))
                .collect(Collectors.toList());

        participantRepository.saveAll(participants);
    }

    public List<MatchDto> getPesonalMatches(UUID userId, String month){
        LocalDate[] dates = parseMonth(month);
        return matchRepository.findMatchesByUserIdAndMonth(userId, dates[0], dates[1]);
    }

    public List<MatchDto> getTeamMatches(Integer teamId, String month){
        LocalDate[] dates = parseMonth(month);
        return matchRepository.findMatchesByTeamIdAndMonth(teamId, dates[0], dates[1]);
    }

    private LocalDate[] parseMonth(String month){
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthValue = Integer.parseInt(parts[1]);

        LocalDate[] dates = new LocalDate[2];

        LocalDate startOfMonth = LocalDate.of(year, monthValue, 1);
        LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());

        dates[0] = startOfMonth;
        dates[1] = endOfMonth;

        return dates;
    }

    public MatchDetailResponse getMatchDetail(UUID userId, Integer matchId){
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new InternalServerException(BaseResponseStatus.MATCH_NOT_FOUND));
        List<ParticipantDto> participantDtos = new ArrayList<>();

        if(match.getTeamId() != null) {
            participantDtos = teamRepository.findParticipantsByUserIdAndMatchId(userId, matchId);
        }

        List<GameReportDto> gameReportDtos = quarterRepository.findGameReportByUserIdAndMatchId(userId, matchId);

        return new MatchDetailResponse(participantDtos, gameReportDtos);
    }

    @Transactional
    public void updatePersonalMatch(UpdatePersonalMatchRequest req){
        Match match = matchRepository.findById(req.getMatchId())
                .orElseThrow(() -> new InternalServerException(BaseResponseStatus.MATCH_NOT_FOUND));

        match.setMatchDate(req.getMatchDate());
        match.setStartTime(req.getStartTime());
        match.setEndTime(req.getEndTime());
        match.setMatchName(req.getMatchName());
    }

    @Transactional
    public void updateTeamMatch(UpdateTeamMatchRequest req){
        Match match = matchRepository.findById(req.getMatchId())
                .orElseThrow(() -> new InternalServerException(BaseResponseStatus.MATCH_NOT_FOUND));

        match.setMatchDate(req.getMatchDate());
        match.setStartTime(req.getStartTime());
        match.setEndTime(req.getEndTime());
        match.setMatchName(req.getMatchName());

        Integer matchId = match.getMatchId();

        participantRepository.deleteAllByMatchId(matchId);

        List<UUID> userIds = teamMemberRepository.findUserIdsByTeamMemberIds(req.getParticipantList());

        List<Participant> participants = userIds.stream()
                .map(userId -> new Participant(userId, matchId, "팀원"))
                .collect(Collectors.toList());

        participantRepository.saveAll(participants);
    }

    public GetMatchListByDateResponse getMatchListByDate(UUID userId, GetMatchListByDateRequest req){
        List<MatchDto> matchList = matchRepository.findMatchesByUserIdAndMatchDates(userId, req.getDates());

        return new GetMatchListByDateResponse(matchList);
    }

}
