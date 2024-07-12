package searchengine.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "jsoup-settings")
public class JsoupSettings {
    private String agent;
    private String referrer;
    @Min(500)
    @Max(10000)
    private int delay;

    {
        this.agent = "";
        this.referrer = "";
        this.delay = 1000;
    }
}
