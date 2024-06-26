package searchengine.units;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.managers.PageLinksCache;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("\"LinksCache\" unit test")
class LinksCacheTest {
    PageLinksCache cache = new PageLinksCache();

    @Test
    @DisplayName("Recount test")
    void testContainsLink() {
        String testString = "Hello5";
        int firstSize = 7;

        cache.containsLink("Hello1");
        cache.containsLink("Hello2");
        cache.containsLink("Hello3");
        cache.containsLink(testString);
        cache.containsLink(testString);
        cache.containsLink(testString);
        cache.containsLink("Hello7");
        cache.containsLink("Hello8");
        cache.containsLink("Hello9");

        assertEquals(firstSize, cache.getSize());
    }
}