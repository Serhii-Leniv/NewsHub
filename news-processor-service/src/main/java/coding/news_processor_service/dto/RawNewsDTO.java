package coding.news_processor_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawNewsDTO {
    private String source;
    private String title;
    private String description;
    private String link;
    private String pubDate;
}
