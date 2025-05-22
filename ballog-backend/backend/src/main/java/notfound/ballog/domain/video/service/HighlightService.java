package notfound.ballog.domain.video.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.domain.match.entity.Match;
import notfound.ballog.domain.match.repository.MatchRepository;
import notfound.ballog.domain.video.dto.HighlightDto;
import notfound.ballog.domain.video.entity.Highlight;
import notfound.ballog.domain.video.entity.Like;
import notfound.ballog.domain.video.entity.Video;
import notfound.ballog.domain.video.repository.HighlightRepository;
import notfound.ballog.domain.video.repository.LikeRepository;
import notfound.ballog.domain.video.repository.VideoRepository;
import notfound.ballog.domain.video.request.AddHighlightRequest;
import notfound.ballog.domain.video.request.UpdateHighlightRequest;
import notfound.ballog.domain.video.request.UpdateLikeRequest;
import notfound.ballog.domain.video.response.AddHighlightResponse;
import notfound.ballog.domain.video.response.ExtractHighlightResponse;
import notfound.ballog.domain.video.response.GetLikeResponse;
import notfound.ballog.exception.DuplicateDataException;
import notfound.ballog.exception.NotFoundException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HighlightService {

    private final MatchRepository matchRepository;
    private final LikeRepository likeRepository;
    private final HighlightRepository highlightRepository;
    private final VideoRepository videoRepository;
    private final WebClient webClient;

    @Transactional
    public void updateHighlight(UpdateHighlightRequest request) {
        Highlight highlight = highlightRepository.findById(request.getHighlightId())
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.HIGHLIGHT_NOT_FOUND));

        highlight.update(request);

        highlightRepository.save(highlight);
    }

    @Transactional
    public void deleteHighlight(Integer highlightId) {
        Highlight highlight = highlightRepository.findById(highlightId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.HIGHLIGHT_NOT_FOUND));

        highlight.delete();

        highlightRepository.save(highlight);
    }

    @Transactional
    public AddHighlightResponse addHighlight(AddHighlightRequest request) {
        Video video = videoRepository.findById(request.getVideoId())
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.VIDEO_NOT_FOUND));

        Highlight highlight = Highlight.toEntity(video, request.getHighlightName(), request.getStartTime(), request.getEndTime());

        Highlight savedHighlight = highlightRepository.save(highlight);

        return AddHighlightResponse.of(savedHighlight.getHighlightId());
    }

    @Transactional
    public void extractHighlight(Integer videoId, MultipartFile file) throws IOException {
        // Video 조회
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.VIDEO_NOT_FOUND));

        // Highlight 조회(Highlight가 하나도 없을 때만 자동 추출)
        Optional<Highlight> existingHighlight = highlightRepository.findByVideo_VideoIdAndDeletedFalse(videoId);
        if (existingHighlight.isPresent()) {
            throw new DuplicateDataException(BaseResponseStatus.HIGHLIGHT_ALREADY_EXIST);
        }

        // 파일을 담은 form-data 생성
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file",
                new ByteArrayResource(file.getBytes()) {
                    @Override public String getFilename() {
                        return file.getOriginalFilename();
                    }
                })
                .header("Content-Type", file.getContentType());

        // fastAPI로 post 요청
        ExtractHighlightResponse highlightResponse = webClient.post()
                .uri("/api/v1/videos/highlight/extract")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(ExtractHighlightResponse.class)
                .block();

        // DB에 하이라이트 저장
        List<HighlightDto> highlightList = null;
        if (highlightResponse != null) {
            highlightList = highlightResponse.getHighlightList();
            log.info("하이라이트 리스트--------------- {}", highlightList);
        }

        if (highlightList != null) {
            log.info("하이라이트 갯수--------------- {}", highlightList.size());
            for (HighlightDto highlightDto : highlightList) {
                Highlight highlight = Highlight.of(video, highlightDto);

                highlightRepository.save(highlight);
            }
        } else {
            throw new NotFoundException(BaseResponseStatus.HIGHLIGHT_EXTRACT_FAIL);
        }
    }


    @Transactional
    public void updateLikes(UUID userId, @Valid UpdateLikeRequest request) {
        List<Integer> highlightIds = request.getHighlightIds();

        // 모든 하이라이트가 존재하는지 먼저 확인
        List<Highlight> highlights = highlightRepository.findAllById(highlightIds);
        if (highlights.size() != highlightIds.size()) {
            throw new NotFoundException(BaseResponseStatus.HIGHLIGHT_NOT_FOUND);
        }

        // 이 사용자와 이 하이라이트들에 대한 기존 좋아요 가져오기
        List<Like> existingLikes = likeRepository.findAllByLikedUserIdAndHighlightIdIn(userId, highlightIds);

        // 빠른 조회를 위한 맵 생성
        Map<Integer, Like> likeMap = existingLikes.stream()
                .collect(Collectors.toMap(Like::getHighlightId, like -> like));

        List<Like> likesToSave = new ArrayList<>();

        for (Integer highlightId : highlightIds) {
            if (likeMap.containsKey(highlightId)) {
                // 기존 좋아요 토글
                Like existingLike = likeMap.get(highlightId);
                existingLike.toggleLikeStatus();
                likesToSave.add(existingLike);
            } else {
                // 새 좋아요 생성
                Like newLike = Like.builder()
                        .highlightId(highlightId)
                        .likedUserId(userId)
                        .isLiked(true)
                        .build();
                likesToSave.add(newLike);
            }
        }

        // 모든 좋아요를 일괄 저장
        likeRepository.saveAll(likesToSave);
    }


    @Transactional
    public GetLikeResponse getLikedHighlights(UUID userId, Integer cursorId, Pageable pageable) {
        // 1. Slice 객체로 좋아요 데이터 조회
        Slice<Like> likesSlice = likeRepository.findLikedHighlightsWithCursor(userId, cursorId, pageable);

        List<Like> likes = likesSlice.getContent();
        if (likes.isEmpty()) {
            return GetLikeResponse.builder()
                    .highlights(Collections.emptyList())
                    .hasNext(false)
                    .nextCursorId(null)
                    .build();
        }

        // 2. 다음 페이지 여부 및 다음 커서 ID 설정
        boolean hasNext = likesSlice.hasNext();
        Integer nextCursorId = hasNext ? likes.get(likes.size() - 1).getHighlightId() : null;

        // 3. 하이라이트 ID 추출
        List<Integer> highlightIds = likes.stream()
                .map(Like::getHighlightId)
                .collect(Collectors.toList());

        // 4. 하이라이트 정보 조회
        List<Highlight> highlights = highlightRepository.findAllById(highlightIds);

        // 5. 하이라이트에서 비디오 ID 추출
        List<Integer> videoIds = highlights.stream()
                .map(highlight -> highlight.getVideo().getVideoId())
                .distinct()
                .collect(Collectors.toList());

        // 6. 비디오 정보 조회
        List<Video> videos = videoRepository.findAllById(videoIds);

        // 7. 비디오에서 매치 ID 추출
        List<Integer> matchIds = videos.stream()
                .map(video -> video.getMatch().getMatchId())
                .distinct()
                .collect(Collectors.toList());

        // 8. 매치 정보 조회
        List<Match> matches = matchRepository.findAllById(matchIds);

        // 9. 빠른 조회를 위한 맵 생성
        Map<Integer, Highlight> highlightMap = highlights.stream()
                .collect(Collectors.toMap(Highlight::getHighlightId, h -> h));

        Map<Integer, Video> videoMap = videos.stream()
                .collect(Collectors.toMap(Video::getVideoId, v -> v));

        Map<Integer, Match> matchMap = matches.stream()
                .collect(Collectors.toMap(Match::getMatchId, m -> m));

        // 10. 응답 구성
        List<GetLikeResponse.LikedHighlightInfo> highlightInfos = new ArrayList<>();

        for (Like like : likes) {
            Highlight highlight = highlightMap.get(like.getHighlightId());
            if (highlight == null) continue;

            Video video = videoMap.get(highlight.getVideo().getVideoId());
            if (video == null) continue;

            Match match = matchMap.get(video.getMatch().getMatchId());
            if (match == null) continue;

            GetLikeResponse.LikedHighlightInfo info = GetLikeResponse.LikedHighlightInfo.builder()
                    .matchId(match.getMatchId())
                    .matchName(match.getMatchName())
                    .matchDate(match.getMatchDate().toString())
                    .startTime(match.getStartTime() != null ? match.getStartTime().toString() : null)
                    .endTime(match.getEndTime() != null ? match.getEndTime().toString() : null)
                    .highlightName(highlight.getHighlightName())
                    .highlightStartTime(highlight.getStartTime().toString())
                    .quarterNumber(video.getQuarterNumber())
                    .build();

            highlightInfos.add(info);
        }

        return GetLikeResponse.builder()
                .highlights(highlightInfos)
                .hasNext(hasNext)
                .nextCursorId(nextCursorId)
                .build();
    }
}
