package coding.news_processor_service.service;


import coding.news_processor_service.dto.ProcessedNewsDTO;
import coding.news_processor_service.dto.RawNewsDTO;
import coding.news_processor_service.entity.NewsArticle;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NewsProcessor {

    private final NewsRepository newsRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, ProcessedNewsDTO> kafkaTemplate;

    public void process(RawNewsDTO dto) {
        // Хеш для перевірки дублікатів
        String hash = DigestUtils.sha256Hex(dto.getSource() + dto.getTitle());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(hash))) return;

        // Очистка HTML
        String cleanDesc = Jsoup.parse(dto.getDescription()).text();

        // Теги і категорія
        List<String> tags = extractTags(cleanDesc);
        String category = determineCategory(tags);

        // Зберегти у БД
        NewsArticle article = new NewsArticle(dto.getTitle(), cleanDesc, dto.getLink(), dto.getSource(), category, tags, dto.getPubDate());
        newsRepository.save(article);

        // Зберегти хеш у Redis на 24 год
        redisTemplate.opsForValue().set(hash, "1", Duration.ofHours(24));

        // Відправити в Kafka
        kafkaTemplate.send("processed-news", new ProcessedNewsDTO(article.getId(), category, tags));
    }

    private List<String> extractTags(String text) {
        // Дуже простий підхід — частотний словник без стоп-слів
        Set<String> stopWords = Set.of("the", "and", "for", "with", "this", "that", "a", "an", "in", "on", "at", "of", "to", "is", "was", "are");
        Map<String, Integer> freq = new HashMap<>();
        for (String word : text.toLowerCase().split("\\W+")) {
            if (!stopWords.contains(word) && word.length() > 3)
                freq.put(word, freq.getOrDefault(word, 0) + 1);
        }
        return freq.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    private String determineCategory(List<String> tags) {
        if (tags.contains("war") || tags.contains("conflict")) return "Politics";
        if (tags.contains("tech") || tags.contains("software")) return "Technology";
        if (tags.contains("football") || tags.contains("match")) return "Sport";
        return "General";
    }
}
