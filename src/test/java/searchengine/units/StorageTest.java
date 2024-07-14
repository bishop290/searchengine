package searchengine.units;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.integration.tools.DbHelper;
import searchengine.managers.Storage;
import searchengine.model.SiteEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@RequiredArgsConstructor
@DisplayName("\"Storage\" unit test")
class StorageTest {
    private Storage storage;

    @BeforeEach
    public void init() {
        storage = new Storage();
    }

    @Test
    @DisplayName("Recount test")
    void testContainsLink() {
        String testString = "Hello5";
        int firstSize = 7;
        storage.containsLink("Hello1");
        storage.containsLink("Hello2");
        storage.containsLink("Hello3");
        storage.containsLink(testString);
        storage.containsLink(testString);
        storage.containsLink(testString);
        storage.containsLink("Hello7");
        storage.containsLink("Hello8");
        storage.containsLink("Hello9");
        assertEquals(firstSize, storage.getLinks().size());
    }

    @Test
    @DisplayName("Test add to lemmas")
    void testAddToLemmas() {
        int expectedSize = 3;
        int expectedFrequency = 2;
        SiteEntity site = DbHelper.getSiteEntity();
        storage.addLemma("злая белка", site);
        storage.addLemma("добрая белка", site);
        storage.addLemma("нейтральная белка", site);
        storage.addLemma("злая белка", site);
        assertEquals(expectedSize, storage.getLemmas().size());
        assertEquals(expectedFrequency, storage.getLemmas().get("злая белка").getFrequency());
    }
}