package coding.news_collector_service.Service;

import coding.news_collector_service.dto.RawNewsDTO;
import coding.news_collector_service.kafka.NewsProducer;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class NewsCollectorService {

    private final NewsProducer newsProducer;
    private static final String RSS_URL = "http://feeds.bbci.co.uk/news/world/rss.xml";

    @Scheduled(fixedRate = 300_000) // кожні 5 хв
    public void fetchNews() {
        try {
            Document rssFeed = Jsoup.connect(RSS_URL).get();
            Elements items = rssFeed.select("item");

            for (Element item : items) {
                RawNewsDTO news = new RawNewsDTO();
                news.setSource("BBC");
                news.setTitle(item.selectFirst("title").text());
                news.setDescription(item.selectFirst("description").text());
                news.setLink(item.selectFirst("link").text());
                news.setPubDate(item.selectFirst("pubDate").text());

                newsProducer.send(news);
            }

        } catch (IOException e) {
            System.err.println("Error fetching/parsing RSS: " + e.getMessage());
        }
    }
}
