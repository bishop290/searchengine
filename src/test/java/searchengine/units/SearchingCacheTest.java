package searchengine.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.components.SearchingCache;
import searchengine.config.SearchSettings;
import searchengine.dto.searching.SearchResponse;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void add() {
        cache.add("запрос", new SearchResponse(true, 0, new ArrayList<>()));
        assertEquals(1, cache.getWeight().size());
        assertEquals(1, cache.getData().size());
        cache.add("запрос", new SearchResponse(true, 0, new ArrayList<>()));
        assertEquals(1, cache.getWeight().size());
        assertEquals(1, cache.getData().size());
        assertEquals(2, cache.getWeight().get("запрос"));
    }

    @Test
    @DisplayName("Check contains data in cache")
    void contains() {
        cache.add("запрос", new SearchResponse(true, 0, new ArrayList<>()));
        assertTrue(cache.contains("запрос"));
    }

    @Test
    @DisplayName("Check response")
    void response() {
        cache.add("запрос", new SearchResponse(true, 5, new ArrayList<>()));
        assertEquals(5, cache.response("запрос").count());
        assertEquals(2, cache.getWeight().get("запрос"));
    }

    @Test
    @DisplayName("Check clear")
    void clear() {
        cache.add("запрос", new SearchResponse(true, 5, new ArrayList<>()));
        cache.add("запрос2", new SearchResponse(true, 5, new ArrayList<>()));
        cache.add("запрос3", new SearchResponse(true, 5, new ArrayList<>()));
        cache.clear();
        assertEquals(0, cache.getData().size());
        assertEquals(0, cache.getWeight().size());
        assertEquals(0, cache.getCounter());
    }

    @Test
    @DisplayName("Check clear low weight")
    void clearLowWeight() {
        settings.setCleanCacheEveryNHits(4);
        settings.setWeightThresholdForCleaning(1);
        cache.add("запрос", new SearchResponse(true, 5, new ArrayList<>()));
        cache.add("запрос", new SearchResponse(true, 5, new ArrayList<>()));
        cache.add("запрос", new SearchResponse(true, 5, new ArrayList<>()));
        cache.add("запрос2", new SearchResponse(true, 5, new ArrayList<>()));
        cache.add("запрос3", new SearchResponse(true, 5, new ArrayList<>()));
        assertEquals(2, cache.getWeight().size());
        assertEquals(2, cache.getData().size());
    }
}