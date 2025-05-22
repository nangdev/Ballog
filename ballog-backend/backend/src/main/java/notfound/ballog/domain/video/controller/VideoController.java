package notfound.ballog.domain.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notfound.ballog.common.response.BaseResponse;
import notfound.ballog.domain.video.request.*;
import notfound.ballog.domain.video.response.AddHighlightResponse;
import notfound.ballog.domain.video.response.AddS3VideoUrlResponse;
import notfound.ballog.domain.video.response.GetLikeResponse;
import notfound.ballog.domain.video.response.GetVideoListResponse;
import notfound.ballog.domain.video.service.HighlightService;
import notfound.ballog.domain.video.service.VideoService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Tag(
        name = "Video"
)
@RestController
@RequestMapping("/v1/videos")
@RequiredArgsConstructor
@Validated
@Slf4j
public class VideoController {

    private final VideoService videoService;

    private final HighlightService highlightService;

    @Operation(summary = "s3 presigned url 발급")
    @PostMapping("/url")
    public BaseResponse<AddS3VideoUrlResponse> addS3Url(@RequestBody AddS3VideoUrlRequest request) {
        AddS3VideoUrlResponse response = videoService.addS3Url(request);

        return BaseResponse.ok(response);
    }


    @Operation(summary = "업로드한 영상 저장")
    @PostMapping()
    public BaseResponse<Void> uploadVideo(@Valid @RequestBody AddVideoRequest request) {
        videoService.uploadVideo(request);

        return BaseResponse.ok();
    }


    @Operation(summary = "쿼터 영상 조회")
    @GetMapping("/{matchId}")
    public BaseResponse<GetVideoListResponse> getVideo(
            @AuthenticationPrincipal UUID userId,
            @PathVariable Integer matchId
    ) {
        GetVideoListResponse response = videoService.getVideo(matchId, userId);

        return BaseResponse.ok(response);
    }


    @Operation(summary = "쿼터 영상 삭제")
    @DeleteMapping("/{videoId}")
    public BaseResponse<Void> deleteVideo(
            @PathVariable
            @NotNull(message = "영상 아이디를 입력하세요.")
            Integer videoId
    ) {
        videoService.deleteVideo(videoId);

        return BaseResponse.ok();
    }


    @Operation(summary = "하이라이트 자동 추출")
    @PostMapping(value = "/highlight/auto",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public BaseResponse<Void> extractHighlight(
            @RequestPart("file") MultipartFile file,
            @RequestPart("videoId") Integer videoId
    ) throws IOException {
        log.info("영상 아이디 ------------ {}", videoId);

        highlightService.extractHighlight(videoId, file);

        return BaseResponse.ok();
    }


    @Operation(summary = "하이라이트 구간 수정")
    @PatchMapping("/highlight")
    public BaseResponse<Void> updateHighlight(@Valid @RequestBody UpdateHighlightRequest request) {
        highlightService.updateHighlight(request);

        return BaseResponse.ok();
    }


    @Operation(summary = "하이라이트 구간 삭제")
    @DeleteMapping("/highlight/{highlightId}")
    public BaseResponse<Void> deleteHighlight(
            @NotNull(message = "하이라이트 아이디를 입력하세요.")
            @PathVariable Integer highlightId
    ){
        highlightService.deleteHighlight(highlightId);

        return BaseResponse.ok();
    }


    @Operation(summary = "하이라이트 구간 추가")
    @PostMapping("/highlight")
    public BaseResponse<AddHighlightResponse> addHighlight(@Valid @RequestBody AddHighlightRequest request) {
        AddHighlightResponse response = highlightService.addHighlight(request);

        return BaseResponse.ok(response);
    }

    @Operation(summary = "하이라이트 좋아요 추가 및 삭제")
    @PostMapping("/likes")
    public BaseResponse<Void> updateLike(
            @AuthenticationPrincipal UUID userId, @Valid @RequestBody UpdateLikeRequest request) {

        highlightService.updateLikes(userId, request);

        return BaseResponse.ok();

    }

    @Operation(
            summary = "하이라이트 좋아요 리스트 커서 조회",
            description = "사용자가 좋아요한 하이라이트 목록을 커서 기반 페이징으로 조회합니다.",
            parameters = {
                    @Parameter(name = "cursorId", description = "마지막으로 조회한 항목의 ID (첫 요청에서는 생략 가능)"),
                    @Parameter(name = "size", description = "한 번에 가져올 항목 수 (기본값: 10)"),
                    @Parameter(name = "sort", description = "정렬 기준 (기본값: highlightId,desc)")
            }
    )
    @GetMapping("/likes")
    public BaseResponse<GetLikeResponse> getLikedHighlights(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) Integer cursorId,
            @PageableDefault(size = 10, sort = "highlightId", direction = Sort.Direction.DESC) Pageable pageable) {

        GetLikeResponse response = highlightService.getLikedHighlights(userId, cursorId, pageable);
        return BaseResponse.ok(response);
    }

}
