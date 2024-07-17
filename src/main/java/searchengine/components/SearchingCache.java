package searchengine.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.SearchSettings;
import searchengine.dto.searching.SearchResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
@RequiredArgsConstructor
public class SearchingCache {
    private final SearchSettings settings;
    private final Map<String, Integer> weight = new ConcurrentHashMap<>();
    private final Map<String, SearchResponse> data = new ConcurrentHashMap<>();
    private int counter = 0;

    public void add(String request, SearchResponse response) {
        clearLowWeight();
        if (weight.containsKey(request)) {
            Integer currentWeight = weight.get(request);
            Integer newWeight =
                    currentWeight == Integer.MAX_VALUE ? Integer.MAX_VALUE : currentWeight + 1;
            weight.put(request, newWeight);
        } else {
            weight.put(request, 1);
            data.put(request, response);
        }
        counter++;
    }

    public boolean contains(String request) {
        return data.containsKey(request);
    }

    public SearchResponse response(String request) {
        SearchResponse response = data.get(request);
        add(request, response);
        return response;
    }

    public void clear() {
        weight.clear();
        data.clear();
        counter = 0;
    }

    private void clearLowWeight() {
        if (counter >= settings.getCleanCacheEveryNHits()) {
            weight.forEach((k, v) -> {
                if (v <= settings.getWeightThresholdForCleaning()) {
                    weight.remove(k);
                    data.remove(k);
                }
            });
            counter = 0;
        }
    }
}
