package notfound.ballog.domain.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.common.utils.S3Util;
import notfound.ballog.domain.auth.entity.Auth;
import notfound.ballog.domain.auth.repository.AuthRepository;
import notfound.ballog.domain.quarter.entity.GameReport;
import notfound.ballog.domain.quarter.repository.GameReportRepository;
import notfound.ballog.domain.user.entity.User;
import notfound.ballog.domain.user.repository.UserRepository;
import notfound.ballog.domain.user.request.AddS3ImageUrlRequest;
import notfound.ballog.domain.user.request.UpdateUserRequest;
import notfound.ballog.domain.user.response.AddS3ImageUrlResponse;
import notfound.ballog.domain.user.response.AiRecommendResponse;
import notfound.ballog.domain.user.response.GetStatisticsResponse;
import notfound.ballog.domain.user.response.GetUserResponse;
import notfound.ballog.exception.NotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    private final AuthRepository authRepository;

    private final GameReportRepository gameReportRepository;

    private final PlayerCardService playerCardService;

    private final OpenAIService openAIService;

    private final NaverCrawlService naverCrawlService;

    private final RedisTemplate<String, AiRecommendResponse> aiRecommendRedisTemplate;

    private final ObjectMapper objectMapper;

    private final S3Util s3Util;

    // 회원가입
    @Transactional
    public User signUp(User newUser) {
        // 유저 저장
        User savedUser = userRepository.save(newUser);

        // 플레이어 카드 생성
        playerCardService.addPlayerCard(savedUser);

        return savedUser;
    }


    public GetUserResponse getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.USER_NOT_FOUND));

        Auth auth = authRepository.findByUser_UserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.USER_NOT_FOUND));

        String email = auth.getEmail();

        return GetUserResponse.of(user, email);
    }


    @Transactional
    public void updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.USER_NOT_FOUND));

        user.updateUser(request);
    }


    @Transactional
    public User reactivateUser(User user) {
        return userRepository.save(user);
    }

    public GetStatisticsResponse getStatistics(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.USER_NOT_FOUND));

        // 1. 최대 5개의 쿼터 리포트 조회
        List<GameReport> gameReportList =
                gameReportRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);

        // 2. 응답에 담을 각 필드별 리스트 초기화
        List<List<List<Integer>>> heatmapList = new ArrayList<>(); // 2D 배열 5개
        List<Double> distanceList       = new ArrayList<>();     // 거리 5개
        List<Double> speedList         = new ArrayList<>();     // 평균 속도 5개
        List<Integer> sprintList        = new ArrayList<>();     // 스프린트 횟수 5개
        List<Integer> heartRateList     = new ArrayList<>();     // 평균 심박수 5개

        // 3. gameReport에서 값 꺼내서 담아주기
        for (GameReport gameReport : gameReportList) {
            Map<String, Object> reportData = gameReport.getReportData();

            List<List<Integer>> heatmap = (List<List<Integer>>) reportData.get("heatmap");
            heatmapList.add(heatmap);

            Number distance = (Number) reportData.get("distance");
            distanceList.add(distance.doubleValue());

            Number speed = (Number) reportData.get("avgSpeed");
            speedList.add(speed.doubleValue());

            Number sprint = (Number) reportData.get("sprint");
            sprintList.add(sprint.intValue());

            Number heartRate = (Number) reportData.get("avgHeartRate");
            heartRateList.add(heartRate.intValue());
        }

        // 4. 히트맵 평균 내기
        List<List<Integer>> averagedHeatmap = new ArrayList<>();

        if (!heatmapList.isEmpty()) {
            // 2) 첫 번째 heatmap의 사이즈를 기준으로 행(row)과 열(col) 크기 결정
            int numRows = heatmapList.get(0).size();
            int numCols = heatmapList.get(0).get(0).size();
            int count = heatmapList.size();

            // 3) 행 단위 순회
            for (int i = 0; i < numRows; i++) {
                List<Integer> averagedRow = new ArrayList<>();

                // 4) 열 단위 순회
                for (int j = 0; j < numCols; j++) {
                    double sum = 0.0;

                    // 5) 5개의 heatmap에서 같은 (i,j) 위치 값을 모두 더하기
                    for (List<List<Integer>> singleHeatmap : heatmapList) {
                        // 만약 일부 heatmap에 해당 위치가 없으면 0 처리
                        List<Integer> row = singleHeatmap.get(i);
                        int value = (j < row.size() ? row.get(j) : 0);
                        sum += value;
                    }

                    // 6) 평균 계산 및 소수점 저장
                    int avgInt = (int) Math.round(sum / count);
                    averagedRow.add(avgInt);
                }

                // 7) 완성된 행을 결과에 추가
                averagedHeatmap.add(averagedRow);
            }
        }

        return GetStatisticsResponse.of(user.getNickname(),
                                        averagedHeatmap,
                                        distanceList,
                                        speedList,
                                        sprintList,
                                        heartRateList);
    }

    public AiRecommendResponse getAiRecommend(UUID userId) {
        // 캐싱된 데이터가 있는지 조회
        String redisKey = "aiRecommend:" + userId;

        AiRecommendResponse cachedResponse = aiRecommendRedisTemplate.opsForValue().get(redisKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.USER_NOT_FOUND));

        List<GameReport> gameReportList =
                gameReportRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);

        if (gameReportList.isEmpty()) {
            throw new NotFoundException(BaseResponseStatus.GAME_REPORT_NOT_FOUND);
        }

        // 게임 리포트 데이터를 JSON 형태로 변환
        List<Map<String, Object>> gameDataList = new ArrayList<>();
        for (GameReport gameReport : gameReportList) {
            gameDataList.add(gameReport.getReportData());
        }

        // 프롬프트 생성
        String prompt =
                "[System] \n" +
                """
                    당신은 풋살 분석 전문가입니다.
                    GPS 히트맵·스프린트 횟수·평균 속도·최고 속도·심박수 데이터를 분석해 선수의 플레이 스타일을 분석하고 발전 방향을 제시합니다.
                    풋살 동호회 선수의 일반적인 경기 데이터는 다음과 같습니다
                """ +
                """
                    {
                        "sprint": 3,
                        "endTime": "21:47",
                        "heatmap": [[4, 2, 2, 4, 2, 2, 0, 8, 10, 8], [4, 2, 2, 6, 6, 4, 2, 0, 8, 0], [2, 2, 6, 8, 8, 6, 2, 0, 0, 0], [2, 4, 6, 8, 8, 6, 2, 0, 0, 0], [4, 6, 8, 8, 10, 8, 2, 0, 0, 0], [4, 4, 6, 8, 10, 8, 6, 0, 0, 0], [2, 6, 8, 10, 10, 8, 6, 4, 0, 0], [4, 4, 6, 8, 8, 6, 4, 2, 0, 0], [2, 4, 6, 6, 8, 6, 6, 4, 0, 0], [0, 4, 6, 6, 8, 6, 4, 2, 0, 0], [2, 4, 4, 2, 6, 4, 2, 2, 0, 0], [4, 6, 6, 4, 2, 2, 2, 2, 0, 0], [2, 4, 4, 2, 4, 2, 0, 2, 0, 0], [0, 2, 2, 0, 2, 2, 2, 0, 0, 0], [0, 2, 0, 0, 0, 0, 2, 2, 0, 0], [0, 0, 0, 0, 0, 0, 0, 2, 0, 0]],
                        "avgSpeed": 2.5013624668121337,
                        "calories": 115,
                        "distance": 0.4587200012207031,
                        "maxSpeed": 10.321200370788574,
                        "startTime": "21:36",
                        "avgHeartRate": 151,
                        "maxHeartRate": 191
                    }
                """ +
                """
                    각 데이터의 heatmap은 경기 중 유저가 어디에 얼마나 머물렀는지 보여주는 히트맵, sprint는 스프린트 횟수, avgSpeed는 평균 속도(km/h), maxSpeed는 최고 속도(km/h),
                    distance는 총 이동 거리(m), avgHeartRate는 평균 심박수, maxHeartRate는 최고 심박수입니다.
                """ +
                "[User] \n" +
                "유저 이름은 " + user.getNickname() + " 입니다." +
                "다음은 풋살 경기에서 얻은 5개의 게임 데이터입니다:\n\n" + gameDataList +
                """
                    \n
                    처음에 제공해준 풋살 동호회 선수의 일반적인 경기 데이터를 기준으로 유저의 경기 데이터를 분석해주세요.
                    분석 시 아래 4가지 데이터를 응답 JSON 구조에 맞게 작성해주세요.
                    1. 활동량 및 포지셔닝 분석 결과(히트맵 데이터 및 총 이동거리 데이터 활용)
                    2. 스프린트 횟수 분석 결과
                    3. 평균/최고 속도 분석 결과
                    4. 체력 분석 결과(평균 심박수, 최고 심박수 데이터 활용)
                """ +
                """
                    그리고 이 데이터들을 바탕으로 유사한 스타일을 가지고 있고 롤모델로 추천할 만한 프로 축구 선수를 추천해주세요.
                    추천한 프로 축구 선수 이름을 네이버에 검색해서 실제 선수명이 맞는지 먼저 확인하세요.
                    검색 결과 실제 선수명이 맞다면 그 선수명을 사용하고 아니라면 네이버가 수정해준 선수명을 사용하세요.
                    해당 축구 선수가 어떤 플레이 스타일인지 알려주세요.
                    유저의 어떤 데이터 때문에 해당 축구 선수를 추천했는지 알려주세요.
                    이 축구 선수처럼 하기 위해선 어떤 점을 보강하면 좋을지 알려주세요.
                """;

        // AI 호출
        Map<String, Object> resp = openAIService.getCompletionFromGPT(prompt);

        // recommendedPlayer 추출
        Map<String, Object> recommendedPlayer = (Map<String, Object>) resp.get("recommendedPlayer");

//        // Wiki 이미지 크롤링(cloudFlare에 막힘)
//        String wikiUrl = (String) recommendedPlayer.get("namuwiki");

//        if (wikiUrl != null && !wikiUrl.isEmpty()) {
//            String imageUrl = wikiCrawlService.getPlayerImageUrl(wikiUrl);
//        }

        // 네이버 이미지 크롤링
        String imageUrl = naverCrawlService.getPlayerImageUrl(recommendedPlayer.get("name").toString());

        if (imageUrl != null) {
            recommendedPlayer.remove("naver");
            recommendedPlayer.put("imageUrl", imageUrl);
        }

        // gpt 분석 결과
        AiRecommendResponse result = objectMapper.convertValue(resp, AiRecommendResponse.class);

        // TTL: 해당일 자정까지
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime nextMid = now.toLocalDate().plusDays(1).atStartOfDay();
        Duration untilMid = Duration.between(now, nextMid);

        aiRecommendRedisTemplate.opsForValue().set(redisKey, result, untilMid);

        return result;
    }

    public AddS3ImageUrlResponse addS3ImageUrl(AddS3ImageUrlRequest request) {
        String originalFileName = request.getFileName();
        String objectKey = s3Util.generateObjectKey(originalFileName, "profileImage");

        // 확장자 추출
        String ext = "";
        String contentType = "image/jpeg";
        int idx = originalFileName.lastIndexOf(".");
        if (idx > 0) {
            ext = originalFileName.substring(idx);
            if (ext.equals(".png")) {
                contentType = "image/png";
            }
            if (ext.equals(".webp")) {
                contentType = "image/webp";
            }
        }

        String presignedUrl = s3Util.generatePresignedUrl(objectKey, contentType);

        return AddS3ImageUrlResponse.of(presignedUrl);
    }
}

