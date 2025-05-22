package notfound.ballog.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class NaverCrawlService {

    // 네이버 검색 URL 템플릿 (query 파라미터만 URLEncoder로 교체)
    private static final String NAVER_SEARCH_URL =
            "https://search.naver.com/search.naver?where=nexearch&query=%s";

//    // 브라우저 위장용 User-Agent
//    private static final String USER_AGENT =
//            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
//                    "(KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36";

    /**
     * 주어진 선수 이름으로 네이버 검색을 수행해,
     * 검색 결과 상단 인물 박스의 이미지를 리턴합니다.
     *
     * @param playerName 검색할 선수 이름
     * @return 이미지 URL 또는 이외(null)일 경우 null
     */
    public String getPlayerImageUrl(String playerName) {
        log.info("▶ 네이버 검색어: {}", playerName);
        try {
            // 1) 검색어 인코딩
            String encoded = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
            String url = String.format(NAVER_SEARCH_URL, encoded);

            // 2) 페이지 요청 (timeout 5초, referrer 포함)
            Document doc = Jsoup.connect(url)
//                    .userAgent(USER_AGENT)
                    .referrer("https://search.naver.com")
                    .timeout(5_000)
                    .get();

            // 3) 인물 박스 영역 내 이미지 선택
            //    (section.case_normal._au_people_content_wrap img._img)
            Element img = doc.selectFirst(
                    "section.case_normal._au_people_content_wrap img._img"
            );

            if (img == null) {
                log.warn("네이버 검색 결과에서 이미지 태그를 찾지 못했습니다: {}", playerName);
                return null;
            }

            // 4) data-src (지연 로딩) 우선, 없으면 src 사용
            String dataSrc = img.attr("data-src");
            if (!dataSrc.isEmpty()) {
                return normalizeUrl(dataSrc);
            }

            String src = img.attr("src");
            if (!src.isEmpty()) {
                return normalizeUrl(src);
            }

            log.warn("이미지 URL 속성(data-src/src)이 비어있습니다: {}", playerName);
            return null;
        } catch (IOException e) {
            log.error("네이버 이미지 크롤링 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * //img 경로나 // 로 시작하는 경우 https:를 붙여서 절대경로로 변환
     */
    private String normalizeUrl(String raw) {
        if (raw.startsWith("//")) {
            return "https:" + raw;
        }
        return raw;
    }
}
