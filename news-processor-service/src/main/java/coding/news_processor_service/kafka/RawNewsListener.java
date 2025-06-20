package coding.news_processor_service.kafka;


import coding.news_processor_service.dto.RawNewsDTO;
import coding.news_processor_service.service.NewsProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RawNewsListener {

    private final NewsProcessor processor;

    @KafkaListener(topics = "raw-news", groupId = "news-processor-group")
    public void consume(RawNewsDTO rawNews) {
        processor.process(rawNews);
    }
}
