package notfound.ballog.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponse;
import notfound.ballog.domain.user.request.AddS3ImageUrlRequest;
import notfound.ballog.domain.user.request.UpdateUserRequest;
import notfound.ballog.domain.user.response.*;
import notfound.ballog.domain.user.service.PlayerCardService;
import notfound.ballog.domain.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "User"
)
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final PlayerCardService playerCardService;

    @Operation(summary = "유저 정보 조회")
    @GetMapping()
    public BaseResponse<GetUserResponse> getUser(@AuthenticationPrincipal UUID userId) {
        GetUserResponse response = userService.getUser(userId);

        return BaseResponse.ok(response);
    }

    @Operation(summary = "유저 정보 수정")
    @PatchMapping()
    public BaseResponse<Void> updateUser(@AuthenticationPrincipal UUID userId,
                                         @Valid @RequestBody UpdateUserRequest request
    ) {
        userService.updateUser(userId, request);

        return BaseResponse.ok();
    }

    @Operation(summary = "내 선수카드 조회")
    @GetMapping("/player-card")
    public BaseResponse<GetPlayerCardResponse> getMyPlayerCard(@AuthenticationPrincipal UUID userId) {
        GetPlayerCardResponse response = playerCardService.getPlayerCard(userId);

        return BaseResponse.ok(response);
    }

    @Operation(summary = "최근 5쿼터 데이터 조회")
    @GetMapping("/statistics")
    public BaseResponse<GetStatisticsResponse> getStatistics(@AuthenticationPrincipal UUID userId) {
        GetStatisticsResponse response = userService.getStatistics(userId);

        return BaseResponse.ok(response);
    }

    @Operation(summary = "AI 리포트 발급")
    @PostMapping("/ai-recommend")
    public BaseResponse<AiRecommendResponse> getAiRecommend(@AuthenticationPrincipal UUID userId) {


        return BaseResponse.ok(userService.getAiRecommend(userId));
    }

    @Operation(summary = "presignedUrl 발급")
    @PostMapping("/presigned-url")
    public BaseResponse<AddS3ImageUrlResponse> addS3ImageUrl(@Valid @RequestBody AddS3ImageUrlRequest request) {
        AddS3ImageUrlResponse response = userService.addS3ImageUrl(request);
        return BaseResponse.ok(response);
    }

}
