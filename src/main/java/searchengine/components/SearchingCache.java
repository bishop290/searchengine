package searchengine.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.SearchSettings;
import searchengine.dto.searching.SearchRequest;
import searchengine.dto.searching.SearchResponse;
import searchengine.dto.searching.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
@RequiredArgsConstructor
public class SearchingCache {
    private static final String DEFAULT = "All";
    private final SearchSettings settings;
    private final Map<String, Integer> weight = new ConcurrentHashMap<>();
    private final Map<String, SearchResponse> data = new ConcurrentHashMap<>();
    private int counter = 0;

    public void add(SearchRequest request, SearchResponse response) {
        String site = DEFAULT;
        if (request.site() != null) {
            site = request.site();
        }
        addToCache(site + request.query(), response);
    }

    public SearchResponse get(SearchRequest request) {
        String site = DEFAULT;
        if (request.site() != null) {
            site = request.site();
        }
        String key = site + request.query();
        if (data.containsKey(key)) {
            return getResponse(key, request);
        }
        return null;
    }

    public void clear() {
        weight.clear();
        data.clear();
        counter = 0;
    }

    private SearchResponse getResponse(String key, SearchRequest request) {
        SearchResponse response = data.get(key);
        addToCache(key, response);
        if (response.count() == 0) {
            return new SearchResponse(true, 0, new ArrayList<>());
        }
        return new SearchResponse(true, response.count(), getPieceOfSnippets(request, response));
    }

    private List<Snippet> getPieceOfSnippets(SearchRequest request, SearchResponse response) {
        int startIndex = Math.max(request.offset(), 0);
        startIndex = Math.min(startIndex, response.data().size() - 1);
        int endIndex = startIndex + request.limit();
        endIndex = Math.min(endIndex, response.data().size());
        return new ArrayList<>(response.data().subList(startIndex, endIndex));
    }

    private void addToCache(String request, SearchResponse response) {
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
