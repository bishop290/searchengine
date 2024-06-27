package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jsoup-settings")
public class JsoupSettings {
    private String agent;
    private String referrer;
    private int delay;

    {
        this.agent = "";
        this.referrer = "";
        this.delay = 1000;
    }
}
