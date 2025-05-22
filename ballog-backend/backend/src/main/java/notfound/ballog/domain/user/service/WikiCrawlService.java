package notfound.ballog.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WikiCrawlService {

    public String getPlayerImageUrl(String wikiUrl) {
        try {
            Connection.Response res = Jsoup.connect(wikiUrl)
                    .ignoreHttpErrors(true)
                    .timeout(10_000)
                    .execute();
            log.info("▶ HTTP Status: {}", res.statusCode());

            String html = res.body();
            log.info("▶ HTML snippet:\n{}", html.length()>1000
                    ? html.substring(0,1000)+"…"
                    : html);

            Document doc = res.parse();
            // style="...width:XYZpx..." 에서 XYZ가 395~405 사이인 div들만 선택
            Elements imgs = doc.select(
                    "div[style~=(?i)width\\s*:\\s*(39[5-9]|40[0-5])px] img[src]:not([src^=data:])"
            );
            log.info("▶ Matched img count: {}", imgs.size());

            for (Element img : imgs) {
                String src = img.attr("src");
                log.info("▶ Found img[src] in nested parse: {}", src);
                return src.startsWith("//") ? "https:" + src : src;
            }

            log.warn("❌ 크롤링 fallback 실패: {}", wikiUrl);
            return null;
        } catch (IOException e) {
            log.error("나무위키 이미지 크롤링 중 에러 발생 URL={}", wikiUrl, e);
            return null;
        }
    }
}