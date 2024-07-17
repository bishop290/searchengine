package searchengine.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class IndexingSettings {
    @Min(1)
    @Max(10000)
    Integer numberOfPagesToIndexAtOneTime;

    {
        this.numberOfPagesToIndexAtOneTime = 50;
    }
}
