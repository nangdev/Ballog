package notfound.ballog.domain.match.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponse;
import notfound.ballog.domain.match.request.*;
import notfound.ballog.domain.match.response.GetMatchListByDateResponse;
import notfound.ballog.domain.match.response.MatchDetailResponse;
import notfound.ballog.domain.match.response.PersonalMatchListResponse;
import notfound.ballog.domain.match.response.TeamMatchListResponse;
import notfound.ballog.domain.match.service.MatchService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/matches")
@Tag(name = "Match", description = "경기 관련 API")
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/me")
    @Operation(summary = "개인 경기 추가", description = "개인 경기 기록을 추가합니다.")
    public BaseResponse<Void> addPesonalMatch(@AuthenticationPrincipal UUID userId, @RequestBody PersonalMatchAddRequest pesonalMatchAddRequest) {
        matchService.addPesonalMatch(userId ,pesonalMatchAddRequest);
        return BaseResponse.ok();
    }

    @PostMapping("/teams")
    @Operation(summary = "팀 경기 추가", description = "팀 경기 기록을 추가합니다.")
    public BaseResponse<Void> addTeamMatch(@RequestBody TeamMatchAddRequest teamMatchAddRequest) {
        matchService.addTeamMatch(teamMatchAddRequest);
        return BaseResponse.ok();
    }

    @GetMapping("/me")
    @Operation(summary = "개인 경기 목록 조회", description = "월별 개인 경기 목록을 조회합니다.")
    public BaseResponse<PersonalMatchListResponse> getPesonalMatches(@AuthenticationPrincipal UUID userId, @RequestParam("month") String month) {
        PersonalMatchListResponse response = new PersonalMatchListResponse(matchService.getPesonalMatches(userId,month));
        return BaseResponse.ok(response);
    }

    @GetMapping("/teams/{teamId}")
    @Operation(summary = "팀 경기 목록 조회", description = "월별 팀 경기 목록을 조회합니다.")
    public BaseResponse<TeamMatchListResponse> getTeamMatches(@PathVariable Integer teamId, @RequestParam("month") String month) {
        TeamMatchListResponse response = new TeamMatchListResponse(matchService.getTeamMatches(teamId,month));
        return BaseResponse.ok(response);
    }

    @GetMapping("/{matchId}")
    @Operation(summary = "경기 상세 조회", description = "경기 ID로 경기 상세 정보를 조회합니다.")
    public BaseResponse<MatchDetailResponse> getMatchDetail(@AuthenticationPrincipal UUID userId, @PathVariable Integer matchId){
        return BaseResponse.ok(matchService.getMatchDetail(userId, matchId));
    }

    @PatchMapping("/me")
    @Operation(summary = "경기 일정 수정")
    public BaseResponse<Void> updatePersonalMatch(@RequestBody UpdatePersonalMatchRequest updatePersonalMatchRequest){
        matchService.updatePersonalMatch(updatePersonalMatchRequest);
        return BaseResponse.ok();
    }

    @PatchMapping("/teams")
    @Operation(summary = "경기 일정 수정")
    public BaseResponse<Void> updatePersonalMatch(@RequestBody UpdateTeamMatchRequest updateTeamMatchRequest){
        matchService.updateTeamMatch(updateTeamMatchRequest);
        return BaseResponse.ok();
    }

    @PostMapping("/date")
    @Operation(summary = "날짜별 모든 매치 리스트 조회")
    public BaseResponse<GetMatchListByDateResponse> getMatchListByDate(@AuthenticationPrincipal UUID userId, @RequestBody GetMatchListByDateRequest getMatchListByDateRequest){
        return BaseResponse.ok(matchService.getMatchListByDate(userId, getMatchListByDateRequest));
    }

}
