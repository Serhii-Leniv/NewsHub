package coding.news_processor_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProcessedNewsDTO {
    private Long id;
    private String category;
    private List<String> tags;
}
