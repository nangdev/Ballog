package notfound.ballog.domain.quarter.service;

import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.domain.match.repository.MatchRepository;
import notfound.ballog.domain.quarter.dto.ReportData;
import notfound.ballog.domain.quarter.entity.GameReport;
import notfound.ballog.domain.quarter.entity.Quarter;
import notfound.ballog.domain.quarter.repository.GameReportRepository;
import notfound.ballog.domain.quarter.repository.QuarterRepository;
import notfound.ballog.domain.quarter.request.AddQuarterAndGameReportRequest;
import notfound.ballog.domain.quarter.response.AddQuarterAndGameReportResponse;
import notfound.ballog.domain.user.entity.PlayerCard;
import notfound.ballog.domain.user.repository.PlayerCardRepository;
import notfound.ballog.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuarterService {

    private final QuarterRepository quarterRepository;
    private final MatchRepository matchRepository;
    private final GameReportRepository gameReportRepository;
    private final PlayerCardRepository playerCardRepository;

    @Transactional
    public AddQuarterAndGameReportResponse addQuarterAndGameReport(UUID userId, AddQuarterAndGameReportRequest req){
        AddQuarterAndGameReportResponse resp = matchRepository.findMatchByUserIdAndMatchId(req.getMatchId());

        if(resp.getMatchId() == null){
            // 원래 여기서 매치 추가하고 그 매치 아이디로 쿼터 등록하고 경기 기록 등록 (자동 등록 버전)
            throw new NotFoundException(BaseResponseStatus.QUARTER_MATCH_NOT_FOUND);
        }

        Integer matchId = resp.getMatchId();

        // 쿼터 넘버 뭐가 있는지 확인하기 위해 쿼터 조회
        List<Quarter> existingQuarters = quarterRepository.findAllByMatchId(matchId);

        // 쿼터 넘버, 쿼터 짝으로 맵 생성
        Map<Integer, Quarter> quarterNumberToQuarter = existingQuarters.stream()
                .collect(Collectors.toMap(Quarter::getQuarterNumber, quarter -> quarter));

        List<Quarter> quartersToSave = new ArrayList<>();

        // 반복문 돌면서 만약 요청에 있는 쿼터 넘버가 없다면 저장할 쿼터 목록에 추가
        for (ReportData reportData : req.getReportDataList()) {
            int quarterNumber = reportData.getQuarterNumber();

            if (!quarterNumberToQuarter.containsKey(quarterNumber)) {
                Quarter newQuarter = new Quarter(matchId, quarterNumber);
                quartersToSave.add(newQuarter);
            }
        }

        if (!quartersToSave.isEmpty()) {
            quarterRepository.saveAll(quartersToSave);
        }

        // 요청에 있는 쿼터 넘버들만 리스트로 만들고
        List<Integer> requestedQuarterNumbers = req.getReportDataList().stream()
                .map(ReportData::getQuarterNumber)
                .collect(Collectors.toList());

        // 새로 만든 쿼터, 기존에 있는 쿼터 포함해서 요청에 있는 쿼터 넘버에 걸리는 쿼터들 가져옴
        List<Quarter> requestedQuarters = quarterRepository.findAllByMatchIdAndQuarterNumberIn(
                matchId, requestedQuarterNumbers);

        // 다시 쿼터 넘버, 쿼터 짝으로 맵 생성
        Map<Integer, Quarter> quarterMap = requestedQuarters.stream()
                .collect(Collectors.toMap(Quarter::getQuarterNumber, quarter -> quarter));

        List<GameReport> gameReportsToSave = new ArrayList<>();
        for (ReportData reportData : req.getReportDataList()) {
            Quarter quarter = quarterMap.get(reportData.getQuarterNumber());

            // 기록된 적이 없는 게임레포트만 생성, 유저아이디와 쿼터아이디로 찾았는데 있다면 이미 저장이 된 경기 기록임
            if(!gameReportRepository.existsByUserIdAndQuarterId(userId, quarter.getQuarterId())){
                gameReportsToSave.add(new GameReport(userId, quarter.getQuarterId(), reportData.getGameReportData(), reportData.getGameSide()));
            }
        }

        // 생성된 모든 GameReport 저장
        if (!gameReportsToSave.isEmpty()) {
            gameReportRepository.saveAll(gameReportsToSave);
        }

        // 사용자의 선수 카드 조회
        PlayerCard playerCard = playerCardRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.NOT_FOUND));

        // 각 쿼터별 데이터 계산해서 능력치 추출 하고 평균 총 쿼터 평균 능력치 계산
        Map<String, Integer> abilities = calculateAveragedAbilities(req.getReportDataList());

        // 계산한 값과 기존에 있던값 가중치 부여해서 능려치 업데이트
        updatePlayerCardAbilities(playerCard, abilities);

        playerCardRepository.save(playerCard);

        return resp;
    }
    
    private Map<String, Integer> calculateAveragedAbilities(List<ReportData> reportDataList) {
        Map<String, Double> totalAbilities = new HashMap<>();
        
        // 초기화
        totalAbilities.put("attack", 0.0);
        totalAbilities.put("defense", 0.0);
        totalAbilities.put("speed", 0.0);
        totalAbilities.put("stamina", 0.0);
        totalAbilities.put("recovery", 0.0);
        
        // 각 쿼터별로 능력치 계산해서 합산
        for (ReportData reportData : reportDataList) {
            Map<String, Object> gameReportData = reportData.getGameReportData();
            Map<String, Integer> quarterAbilities = calculateAbilities(gameReportData);
            
            // 능력치 합산
            totalAbilities.replaceAll((k, v) -> totalAbilities.get(k) + quarterAbilities.get(k));
        }
        
        // 평균 계산 및 결과 반환
        Map<String, Integer> averagedAbilities = new HashMap<>();
        int count = Math.max(1, reportDataList.size());
        
        for (String key : totalAbilities.keySet()) {
            double avg = totalAbilities.get(key) / count;
            averagedAbilities.put(key, (int) Math.round(avg));
        }
        
        return averagedAbilities;
    }

    private Map<String, Integer> calculateAbilities(Map<String, Object> data) {
        Map<String, Integer> abilities = new HashMap<>();
        
        // 필요한 데이터 추출
        String startTime = (String) data.get("startTime");
        String endTime = (String) data.get("endTime");
        double distance = ((Number) data.get("distance")).doubleValue();
        double avgSpeed = ((Number) data.get("avgSpeed")).doubleValue();
        double maxSpeed = ((Number) data.get("maxSpeed")).doubleValue();
        int sprint = ((Number) data.get("sprint")).intValue();
        int avgHeartRate = ((Number) data.get("avgHeartRate")).intValue();
        int maxHeartRate = ((Number) data.get("maxHeartRate")).intValue();
        String gameSide = (String) data.get("gameSide");
        
        // 히트맵 데이터 가져오기
        int[][] heatmap = convertToHeatmap(data.get("heatmap"));
        
        // 경기 시간 계산 (분 단위)
        double gameDurationMinutes = calculateGameDuration(startTime, endTime);
        
        // 히트맵 분석 - 진영 점유율 계산
        double[] occupancyRates = calculateOccupancyRates(heatmap, gameSide);
        double opponentSideOccupancyRate = occupancyRates[0]; // 상대 진영 점유율
        double mySideOccupancyRate = occupancyRates[1];       // 자기 진영 점유율
        
        // 공격력 계산
        // (상대진영 점유 비율 * 0.6) + (스프린트 횟수/경기시간(분) * 0.3) + (최고 속도/20 * 0.1) * 100
        double attack = (opponentSideOccupancyRate * 0.6) 
                + ((double)sprint / Math.max(1, gameDurationMinutes) * 0.3) 
                + (maxSpeed / 20.0 * 0.1);
        attack = attack * 100;
        abilities.put("attack", clamp((int)Math.round(attack)));
        
        // 수비력 계산
        // (자기진영 점유 비율 * 0.7) + (스프린트 횟수/경기시간(분) * 0.15) + (이동거리/경기시간(분) * 0.15) * 100
        double defense = (mySideOccupancyRate * 0.7) 
                + ((double)sprint / Math.max(1, gameDurationMinutes) * 0.15) 
                + (distance / Math.max(1, gameDurationMinutes) * 0.15);
        defense = defense * 100;
        abilities.put("defense", clamp((int)Math.round(defense)));
        
        // 스피드 계산
        // (평균 속도/10 * 0.4 + 최고 속도/20 * 0.6) * 100
        double speed = (avgSpeed / 10.0 * 0.4) + (maxSpeed / 20.0 * 0.6);
        speed = speed * 100;
        abilities.put("speed", clamp((int)Math.round(speed)));
        
        // 스태미나 계산
        // (총 운동시간(분)/15 * 0.4 + 이동거리/(경기시간(분) * 80) * 0.4 + (1-(평균 심박수/최대 심박수)) * 0.2) * 100
        double stamina = (gameDurationMinutes / 15.0 * 0.4) 
                + (distance / (Math.max(1, gameDurationMinutes) * 80.0 / 1000.0) * 0.4);
        
        if (maxHeartRate > 0) {
            stamina += ((1.0 - (double)avgHeartRate / maxHeartRate) * 0.2);
        }
        stamina = stamina * 100;
        abilities.put("stamina", clamp((int)Math.round(stamina)));
        
        // 회복력 계산 (체중 제외)
        // (최대 심박수-평균 심박수)/60 * 100
        double recovery = (double)(maxHeartRate - avgHeartRate) / 60.0;
        recovery = recovery * 100;
        abilities.put("recovery", clamp((int)Math.round(recovery)));
        
        return abilities;
    }

    private void updatePlayerCardAbilities(PlayerCard playerCard, Map<String, Integer> abilities) {
        playerCard.setAttack(calculateNewAbilityValue(playerCard.getAttack(), abilities.get("attack")));
        playerCard.setDefense(calculateNewAbilityValue(playerCard.getDefense(), abilities.get("defense")));
        playerCard.setSpeed(calculateNewAbilityValue(playerCard.getSpeed(), abilities.get("speed")));
        playerCard.setStamina(calculateNewAbilityValue(playerCard.getStamina(), abilities.get("stamina")));
        playerCard.setRecovery(calculateNewAbilityValue(playerCard.getRecovery(), abilities.get("recovery")));
    }

    private int calculateNewAbilityValue(int currentValue, int newValue) {
        // 능력치 변화 비율 (기존 값 70%, 새 값 30% 반영)
        double ratio = 0.7;
        
        if (currentValue == 0) {
            // 첫 능력치 설정인 경우 새 값 그대로 사용
            return newValue;
        }
        
        // 기존 값과 새 값의 가중 평균
        return (int) Math.round(currentValue * ratio + newValue * (1 - ratio));
    }

    private double calculateGameDuration(String startTime, String endTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date start = format.parse(startTime);
            Date end = format.parse(endTime);
            
            // 시간 차이 계산 (밀리초)
            long diffInMillis = end.getTime() - start.getTime();
            
            // 음수일 경우(자정을 넘긴 경우) 24시간 추가
            if (diffInMillis < 0) {
                diffInMillis += 24 * 60 * 60 * 1000;
            }
            
            // 분으로 변환
            return diffInMillis / (1000.0 * 60.0);
        } catch (Exception e) {
            // 파싱 에러 시 기본값 반환
            return 15.0; // 기본 15분
        }
    }

    private double[] calculateOccupancyRates(int[][] heatmap, String gameSide) {
        int totalHeatValue = 0;
        int opponentSideHeatValue = 0;
        int mySideHeatValue = 0;
        
        // 히트맵 총합 및 각 진영 합계 계산
        for (int[] arr : heatmap) {
            for (int j = 0; j < arr.length; j++) {
                totalHeatValue += arr[j];

                // 좌측이 본인 진영인 경우
                if ("left".equalsIgnoreCase(gameSide)) {
                    if (j <= 7) { // 0~7번 열은 자기 진영
                        mySideHeatValue += arr[j];
                    } else { // 8~15번 열은 상대 진영
                        opponentSideHeatValue += arr[j];
                    }
                } else { // 우측이 본인 진영인 경우
                    if (j <= 7) { // 0~7번 열은 상대 진영
                        opponentSideHeatValue += arr[j];
                    } else { // 8~15번 열은 자기 진영
                        mySideHeatValue += arr[j];
                    }
                }
            }
        }
        
        // 점유율 계산 (0~1 사이 값)
        double opponentSideOccupancyRate = (totalHeatValue > 0) 
                ? (double) opponentSideHeatValue / totalHeatValue 
                : 0.0;
        double mySideOccupancyRate = (totalHeatValue > 0) 
                ? (double) mySideHeatValue / totalHeatValue 
                : 0.0;
        
        return new double[] {opponentSideOccupancyRate, mySideOccupancyRate};
    }

    @SuppressWarnings("unchecked")
    private int[][] convertToHeatmap(Object heatmapObj) {
        if (heatmapObj instanceof int[][]) {
            return (int[][]) heatmapObj;
        } else if (heatmapObj instanceof List) {
            List<List<Integer>> heatmapList = (List<List<Integer>>) heatmapObj;
            int[][] heatmap = new int[10][16];

            for (int i = 0; i < Math.min(heatmapList.size(), 10); i++) {
                List<Integer> row = heatmapList.get(i);
                for (int j = 0; j < Math.min(row.size(), 16); j++) {
                    heatmap[i][j] = row.get(j);
                }
            }

            return heatmap;
        }

        // 기본 빈 히트맵 반환
        int[][] emptyHeatmap = new int[10][16];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 16; j++) {
                emptyHeatmap[i][j] = 0;
            }
        }
        return emptyHeatmap;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

}
