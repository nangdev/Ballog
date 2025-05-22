package notfound.ballog.domain.video.service;

import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.common.utils.S3Util;
import notfound.ballog.domain.match.entity.Match;
import notfound.ballog.domain.match.repository.MatchRepository;
import notfound.ballog.domain.quarter.repository.QuarterRepository;
import notfound.ballog.domain.video.dto.HighlightDto;
import notfound.ballog.domain.video.dto.VideoDto;
import notfound.ballog.domain.video.entity.Highlight;
import notfound.ballog.domain.video.entity.Like;
import notfound.ballog.domain.video.entity.Video;
import notfound.ballog.domain.video.repository.HighlightRepository;
import notfound.ballog.domain.video.repository.LikeRepository;
import notfound.ballog.domain.video.repository.VideoRepository;
import notfound.ballog.domain.video.request.AddS3VideoUrlRequest;
import notfound.ballog.domain.video.request.AddVideoRequest;
import notfound.ballog.domain.video.response.AddS3VideoUrlResponse;
import notfound.ballog.domain.video.response.GetVideoListResponse;
import notfound.ballog.exception.InternalServerException;
import notfound.ballog.exception.NotFoundException;
import notfound.ballog.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoService {

    private final LikeRepository likeRepository;

    private final VideoRepository videoRepository;

    private final HighlightRepository highlightRepository;

    private final MatchRepository matchRepository;

    private final QuarterRepository quarterRepository;

    private final S3Util s3Util;

    @Transactional
    public AddS3VideoUrlResponse addS3Url(AddS3VideoUrlRequest request) {
        String objectKey = s3Util.generateObjectKey(request.getFileName(), "video");

        String contentType = "video/mp4";

        String presignedUrl = s3Util.generatePresignedUrl(objectKey, contentType);

        // 영상이 저장될 objectUrl 추출
        int idx = presignedUrl.indexOf("?");
        String objectUrl;
        if (idx != -1) {
            objectUrl = presignedUrl.substring(0, idx);
        } else {
            throw new InternalServerException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }

        // 이미 같은 url 주소에 저장된 게 있는지 체크
        Optional<Video> existingVideo = videoRepository.findByVideoUrl(objectUrl);
        if (existingVideo.isPresent()) {
            throw new ValidationException(BaseResponseStatus.VIDEO_ALREADY_EXIST);
        }

        // 영상 데이터 저장
        Video video = Video.ofVideoUrl(objectUrl);
        Video savedVideo = videoRepository.save(video);

        return AddS3VideoUrlResponse.of(presignedUrl, savedVideo.getVideoId());
    }

    @Transactional
    public void uploadVideo(AddVideoRequest request) {
//        // 업로드한 영상 있는지 확인
//        Optional<Video> existingVideo = videoRepository
//                .findByMatch_MatchIdAndQuarterNumberAndDeletedFalse(matchId, quarterNumber);
//        if (existingVideo.isPresent()) {
//            throw new ValidationException(BaseResponseStatus.VIDEO_ALREADY_EXIST);
//        }

        // 이미 저장해둔 영상 찾기
        Video video = videoRepository.findByVideoUrl(request.getVideoUrl())
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.VIDEO_NOT_FOUND));

        // 해당하는 매치 조회
        Match match = matchRepository.findById(request.getMatchId())
                        .orElseThrow(() -> new NotFoundException(BaseResponseStatus.MATCH_NOT_FOUND));

        // Duration 타입으로 변환
        String[] part = request.getDuration().split(":");
        long hours = Long.parseLong(part[0]);
        long minutes = Long.parseLong(part[1]);
        long seconds = Long.parseLong(part[2]);
        Duration videoDuration = Duration.ofHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);

//        Video video = Video.of(match, request.getQuarterNumber(), request.getVideoUrl(), videoDuration);
        video.save(match, request.getQuarterNumber(), request.getVideoUrl(), videoDuration);

        videoRepository.save(video);
    }


    public GetVideoListResponse getVideo(Integer matchId, UUID userId) {
        // 1. 총 쿼터 수 조회
        Integer totalQuarters = quarterRepository.countByMatchId(matchId);

        // 2. 쿼터 영상 조회 -> 없으면 null로 반환
        List<Video> videoList = videoRepository.findAllByMatch_MatchIdAndDeletedFalse(matchId);
        if (videoList.isEmpty()) {
            return GetVideoListResponse.emptyOf(totalQuarters);
        }

        // 3. 각 영상별로 하이라이트 목록과, 각 하이라이트에 대해 사용자가 좋아요를 눌렀는지 여부를 포함한 DTO 생성
        List<VideoDto> videoDtoList = videoList.stream()
                .map(video -> {
                    Integer videoId = video.getVideoId();
                    List<Highlight> highlightList = highlightRepository.findAllByVideo_VideoIdAndDeletedFalse(videoId);

                    // 각 하이라이트별로, 현재 사용자가 좋아요를 눌렀는지 여부를 판단하여 DTO로 변환
                    List<HighlightDto> highlightDtoList = highlightList.stream()
                            .map(highlight -> {
                                // likeRepository를 통해 해당 하이라이트에 대해 사용자가 좋아요를 눌렀는지 조회
                                boolean isLiked = likeRepository.findAllByLikedUserIdAndHighlightIdIn(
                                        userId, List.of(highlight.getHighlightId()))
                                        .stream()
                                        .anyMatch(Like::getIsLiked);
                                // 좋아요 여부를 포함하여 DTO 생성
                                return HighlightDto.of(highlight, isLiked);
                            })
                            .toList();

                    // 영상과 하이라이트 DTO 리스트로 VideoDto 생성
                    return VideoDto.of(video, highlightDtoList);
                })
                .toList();

        // 전체 쿼터 수와 영상 DTO 리스트로 응답 생성
        return GetVideoListResponse.of(totalQuarters, videoDtoList);
    }

    @Transactional
    public void deleteVideo(Integer videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.VIDEO_NOT_FOUND));

        video.delete();

        videoRepository.save(video);
    }
}
