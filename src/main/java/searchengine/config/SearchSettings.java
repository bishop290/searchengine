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
@ConfigurationProperties(prefix = "search-settings")
public class SearchSettings {
    @Min(1)
    @Max(100)
    private int frequencyLimitInPercentage;
    @Min(100)
    @Max(1000)
    private int snippetSize;
    @Min(10)
    @Max(100000)
    private int cleanCacheEveryNAdditions;
    @Min(1)
    @Max(100000)
    private int weightThresholdForCleaning;
}
