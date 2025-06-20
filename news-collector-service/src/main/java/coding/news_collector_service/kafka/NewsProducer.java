package coding.news_collector_service.kafka;

import coding.news_collector_service.dto.RawNewsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsProducer {
    private final KafkaTemplate<String, RawNewsDTO> kafkaTemplate;

    public void send(RawNewsDTO news) {
        kafkaTemplate.send("raw-news", news);
    }
}
