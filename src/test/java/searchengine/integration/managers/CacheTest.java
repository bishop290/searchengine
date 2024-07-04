package searchengine.integration.managers;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.components.PageToDbWorker;
import searchengine.integration.tools.DatabaseWorker;
import searchengine.integration.tools.IntegrationTest;
import searchengine.managers.Cache;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"Cache\" unit test")
class CacheTest {
    private final PageToDbWorker dbWorker;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final EntityManager entityManager;
    private final NamedParameterJdbcTemplate jdbc;
    private Cache cache;

    @BeforeEach
    public void init() {
        cache = new Cache(dbWorker);
    }

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

    @Test
    @DisplayName("Test add to lemmas cache")
    void testAddToLemmasCache() {
        int size = 4;
        int freq = 3;
        SiteEntity site = DatabaseWorker.getSiteEntity();
        LemmaEntity lemma = DatabaseWorker.getLemmaEntity(site, "злая белка", 1);
        List<LemmaEntity> lemmas = new ArrayList<>();
        lemmas.add(DatabaseWorker.getLemmaEntity(site, "ягуар", 1));
        lemmas.add(lemma);
        lemmas.add(lemma);
        lemmas.add(lemma);
        lemmas.add(DatabaseWorker.getLemmaEntity(site, "собака", 1));
        lemmas.add(DatabaseWorker.getLemmaEntity(site, "хомяк", 1));

        cache.addLemmas(lemmas);

        assertEquals(size, cache.getLemmas().size());
        assertEquals(freq, cache.getLemmas().get(lemma.getLemma()).getFrequency());
    }

    @Test
    @DisplayName("Test Update Database from lemmas cache")
    void testUpdateDatabaseFromLemmasCache() {
        int limit = 3;
        int numberOfLemmasInDb = 4;
        int numberOfLemmasInCache = 1;
        SiteEntity site = DatabaseWorker.newSiteEntityFromDb(siteRepository, entityManager);
        LemmaEntity lemma = DatabaseWorker.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        List<LemmaEntity> lemmas1 = new ArrayList<>();
        List<LemmaEntity> lemmas2 = new ArrayList<>();
        lemmas1.add(DatabaseWorker.newLemmaEntityFromDb(site, "ягуар", 1, lemmaRepository, entityManager));
        lemmas1.add(lemma);
        lemmas1.add(lemma);
        lemmas1.add(DatabaseWorker.newLemmaEntityFromDb(site, "собака", 1, lemmaRepository, entityManager));
        lemmas2.add(DatabaseWorker.newLemmaEntityFromDb(site, "хомяк", 1, lemmaRepository, entityManager));

        cache.setLimit(limit);
        cache.addLemmas(lemmas1);
        cache.addLemmas(lemmas2);

        assertEquals(numberOfLemmasInDb, DatabaseWorker.count("lemma", jdbc));
        assertEquals(numberOfLemmasInCache, cache.getLemmas().size());
    }

    @Test
    @DisplayName("Test add indexes with insert to db")
    void testAddIndexes() {
        int limit = 3;
        int numberIndexesFromDb = 4;
        int numberIndexesInCache = 1;
        SiteEntity site = DatabaseWorker.newSiteEntityFromDb(siteRepository, entityManager);
        LemmaEntity lemma = DatabaseWorker.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        PageEntity page1 = DatabaseWorker.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        PageEntity page2 = DatabaseWorker.newPageEntityFromDb(site, "/hello/kitty", pageRepository, entityManager);
        PageEntity page3 = DatabaseWorker.newPageEntityFromDb(site, "/hello/frog", pageRepository, entityManager);
        PageEntity page4 = DatabaseWorker.newPageEntityFromDb(site, "/hello/pig", pageRepository, entityManager);
        PageEntity page5 = DatabaseWorker.newPageEntityFromDb(site, "/hello/main", pageRepository, entityManager);
        List<IndexEntity> indexes1 = new ArrayList<>();
        List<IndexEntity> indexes2 = new ArrayList<>();
        indexes1.add(DatabaseWorker.getIndexEntity(page1, lemma));
        indexes1.add(DatabaseWorker.getIndexEntity(page2, lemma));
        indexes1.add(DatabaseWorker.getIndexEntity(page3, lemma));
        indexes1.add(DatabaseWorker.getIndexEntity(page4, lemma));
        indexes2.add(DatabaseWorker.getIndexEntity(page5, lemma));

        cache.setLimit(limit);
        cache.addIndexes(indexes1);
        cache.addIndexes(indexes2);

        assertEquals(numberIndexesFromDb, DatabaseWorker.count("index", jdbc));
        assertEquals(numberIndexesInCache, cache.getIndexes().size());
    }
}