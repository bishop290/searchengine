package searchengine.dto.searching;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PageData {
    String site;
    String siteName;
    String uri;
    String title;
    String snippet;
    float relevance;
}
