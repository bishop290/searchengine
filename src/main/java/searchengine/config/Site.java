package searchengine.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class Site {
    private String url;
    private String name;
}
