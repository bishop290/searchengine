package searchengine.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.components.SearchingCache;
import searchengine.config.SearchSettings;
import searchengine.dto.searching.SearchRequest;
import searchengine.dto.searching.SearchResponse;
import searchengine.dto.searching.Snippet;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchingCacheTest {
    private SearchingCache cache;
    private SearchSettings settings;

    @BeforeEach
    void init() {
        settings = new SearchSettings();
        settings.setCleanCacheEveryNHits(100);
        settings.setWeightThresholdForCleaning(100);
        cache = new SearchingCache(settings);
    }

    @Test
    @DisplayName("Add to cache")
    void testAdd() {
        cache.add(new SearchRequest("запрос", "сайт", 0, 10),
                new SearchResponse(true, 0, new ArrayList<>()));
        assertEquals(1, cache.getWeight().size());
        assertEquals(1, cache.getData().size());
        cache.add(new SearchRequest("запрос", "сайт", 0, 10),
                new SearchResponse(true, 0, new ArrayList<>()));
        assertEquals(1, cache.getWeight().size());
        assertEquals(1, cache.getData().size());
        assertEquals(2, cache.getWeight().get("сайтзапрос"));
    }

    @Test
    @DisplayName("Get snippets with limit and offset")
    void testGet() {
        Snippet snippet = Snippet.builder().build();
        cache.add(new SearchRequest("запрос1", "сайт", 0, 2),
                new SearchResponse(true, 5,
                        Arrays.asList(snippet, snippet, snippet, snippet, snippet)));
        SearchResponse response1 = cache.get(new SearchRequest("запрос1", "сайт", 0, 2));
        SearchResponse response2 = cache.get(new SearchRequest("запрос1", "сайт", 2, 2));
        SearchResponse response3 = cache.get(new SearchRequest("запрос1", "сайт", 4, 2));
        assertEquals(2, response1.data().size());
        assertEquals(2, response2.data().size());
        assertEquals(1, response3.data().size());
    }

    @Test
    @DisplayName("Check clear")
    void testClear() {
        cache.add(new SearchRequest("запрос1", "сайт", 0, 10),
                new SearchResponse(true, 5, new ArrayList<>()));
        cache.add(new SearchRequest("запрос2", "сайт", 0, 10),
                new SearchResponse(true, 5, new ArrayList<>()));
        cache.add(new SearchRequest("запрос3", "сайт", 0, 10),
                new SearchResponse(true, 5, new ArrayList<>()));
        cache.clear();
        assertEquals(0, cache.getData().size());
        assertEquals(0, cache.getWeight().size());
        assertEquals(0, cache.getCounter());
    }

    @Test
    @DisplayName("Check clear low weight")
    void testClearLowWeight() {
        settings.setCleanCacheEveryNHits(4);
        settings.setWeightThresholdForCleaning(1);
        cache.add(new SearchRequest("запрос1", "сайт", 0, 10),
                new SearchResponse(true, 5, new ArrayList<>()));
        cache.add(new SearchRequest("запрос2", "сайт", 0, 10),
                new SearchResponse(true, 5, new ArrayList<>()));
        cache.add(new SearchRequest("запрос2", "сайт", 0, 10),
                new SearchResponse(true, 5, new ArrayList<>()));
        cache.add(new SearchRequest("запрос4", "сайт", 0, 10),
                new SearchResponse(true, 5, new ArrayList<>()));
        cache.add(new SearchRequest("запрос5", "сайт", 0, 10),
                new SearchResponse(true, 5, new ArrayList<>()));
        assertEquals(2, cache.getWeight().size());
        assertEquals(2, cache.getData().size());
    }
}