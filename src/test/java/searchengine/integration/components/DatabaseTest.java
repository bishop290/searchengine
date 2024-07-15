package searchengine.integration.components;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.components.Database;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.integration.tools.DbHelper;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"Database\" integration tests")
class DatabaseTest extends TestContainer {
    private final NamedParameterJdbcTemplate namedJdbc;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final NamedParameterJdbcTemplate jdbc;
    private Database database;
    private final JdbcTemplate jdbcTemplate;
    private static SitesList sitesList;
    private final EntityManager entityManager;

    @BeforeAll
    public static void setSite() {
        int numberOfSites = 4;
        List<Site> sites = new ArrayList<>();
        for (int i = 0; i < numberOfSites; i++) {
            sites.add(Site.builder().url("www.google.com" + i).name("Google" + i).build());
        }
        sitesList = new SitesList();
        sitesList.setSites(sites);
    }

    @BeforeEach
    public void init() {
        database = new Database(siteRepository, lemmaRepository, pageRepository, indexRepository, jdbcTemplate);
    }

    @Test
    @DisplayName("Site update")
    void testSiteUpdate() {
        String lastError = "last error";
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);

        database.siteUpdate(site, Status.INDEXED, lastError);
        SiteEntity savedSite = DbHelper.get(SiteEntity.class, namedJdbc);

        assertEquals(Status.INDEXED, savedSite.getStatus());
        assertEquals(lastError, savedSite.getLastError());
        assertNotNull(savedSite.getStatusTime());
    }

    @Test
    @DisplayName("Delete Page")
    void testDeletePage() {
        String path = "/google/moogle";
        int frequency = 10;
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        PageEntity page = DbHelper.newPageEntityFromDb(site, path, pageRepository, entityManager);
        LemmaEntity lemma = DbHelper.newLemmaEntityFromDb(site, "ягуар", frequency, lemmaRepository, entityManager);
        LemmaEntity lemma2 = DbHelper.newLemmaEntityFromDb(site, "ягуар", 1, lemmaRepository, entityManager);
        IndexEntity index = DbHelper.newIndexEntityFromDb(page, lemma, indexRepository, entityManager);
        IndexEntity index2 = DbHelper.newIndexEntityFromDb(page, lemma2, indexRepository, entityManager);

        PageEntity newPage = pageRepository.findBySiteAndPath(site, path);
        database.removePage(newPage);

        assertEquals(1, DbHelper.count("site", jdbc));
        assertEquals(1, DbHelper.count("lemma", jdbc));
        assertEquals(0, DbHelper.count("page", jdbc));
        assertEquals(0, DbHelper.count("index", jdbc));

        LemmaEntity siteLemma = DbHelper.get(LemmaEntity.class, jdbc);
        assertEquals(frequency - 1, siteLemma.getFrequency());
    }

    @Test
    @DisplayName("Update lemmas")
    void testUpdateLemmas() {
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        LemmaEntity lemma1 = DbHelper.newLemmaEntityFromDb(site, "ягуар1", 1, lemmaRepository, entityManager);
        LemmaEntity lemma2 = DbHelper.newLemmaEntityFromDb(site, "ягуар2", 1, lemmaRepository, entityManager);
        LemmaEntity lemma3 = DbHelper.newLemmaEntityFromDb(site, "ягуар3", 1, lemmaRepository, entityManager);
        LemmaEntity lemma4 = DbHelper.newLemmaEntityFromDb(site, "ягуар4", 1, lemmaRepository, entityManager);
        lemma1.setFrequency(3);
        lemma2.setFrequency(3);
        lemma3.setFrequency(3);
        lemma4.setFrequency(3);
        List<LemmaEntity> lemmas = new ArrayList<>();
        lemmas.add(lemma1);
        lemmas.add(lemma2);
        lemmas.add(lemma3);
        lemmas.add(lemma4);
        database.updateLemmas(lemmas);
        assertEquals(4, DbHelper.count("lemma", jdbc));
    }

    @Test
    @DisplayName("Insert sites to db")
    public void testInsertSites() {
        int expectedCount = 4;
        List<SiteEntity> sites = new ArrayList<>();
        sites.add(DbHelper.getSiteEntity());
        sites.add(DbHelper.getSiteEntity());
        sites.add(DbHelper.getSiteEntity());
        sites.add(DbHelper.getSiteEntity());
        database.insertSites(sites);
        assertEquals(expectedCount, DbHelper.count("site", jdbc));
    }

    @Test
    @DisplayName("Insert pages to db")
    public void testInsertPages() {
        int expectedCount = 4;
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        List<PageEntity> pages = new ArrayList<>();
        pages.add(DbHelper.getPageEntity(site, "/hello"));
        pages.add(DbHelper.getPageEntity(site, "/hello/kitty"));
        pages.add(DbHelper.getPageEntity(site, "/hello/frog"));
        pages.add(DbHelper.getPageEntity(site, "/hello/pig"));
        database.insertPages(pages);
        assertEquals(expectedCount, DbHelper.count("page", jdbc));
    }

    @Test
    @DisplayName("Insert lemmas to db")
    public void testInsertLemmas() {
        int expectedCount = 4;
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        List<LemmaEntity> lemmas = new ArrayList<>();
        lemmas.add(DbHelper.getLemmaEntity(site, "злая белка", 1));
        lemmas.add(DbHelper.getLemmaEntity(site, "добрая белка", 1));
        lemmas.add(DbHelper.getLemmaEntity(site, "нейтральная белка", 1));
        lemmas.add(DbHelper.getLemmaEntity(site, "странная белка", 1));
        database.insertLemmas(lemmas);
        assertEquals(expectedCount, DbHelper.count("lemma", jdbc));
    }

    @Test
    @DisplayName("Insert indexes to db")
    public void testInsertIndexes() {
        int numberIndexesFromDb = 4;
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        LemmaEntity lemma = DbHelper.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        PageEntity page1 = DbHelper.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        PageEntity page2 = DbHelper.newPageEntityFromDb(site, "/hello/kitty", pageRepository, entityManager);
        PageEntity page3 = DbHelper.newPageEntityFromDb(site, "/hello/frog", pageRepository, entityManager);
        PageEntity page4 = DbHelper.newPageEntityFromDb(site, "/hello/pig", pageRepository, entityManager);
        List<IndexEntity> indexes = new ArrayList<>();
        indexes.add(DbHelper.getIndexEntity(page1, lemma));
        indexes.add(DbHelper.getIndexEntity(page2, lemma));
        indexes.add(DbHelper.getIndexEntity(page3, lemma));
        indexes.add(DbHelper.getIndexEntity(page4, lemma));

        database.insertIndexes(indexes);
        assertEquals(numberIndexesFromDb, DbHelper.count("index", jdbc));
    }

    @Test
    @DisplayName("Get lemmas in hash map")
    public void testGetLemmasInHashMap() {
        int expectedCount = 4;
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site, "добрая белка", 1, lemmaRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site, "нейтральная белка", 1, lemmaRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site, "странная белка", 1, lemmaRepository, entityManager);

        Map<String, LemmaEntity> lemmas = database.lemmas(site);
        assertEquals(expectedCount, lemmas.keySet().size());
    }

    @Test
    @DisplayName("Get pages in Set")
    public void testGetPagesInHashMap() {
        int expectedCount = 4;
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, "/hello/kitty", pageRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, "/hello/frog", pageRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, "/hello/pig", pageRepository, entityManager);

        Set<PageEntity> pages = database.pages(site);
        assertEquals(expectedCount, pages.size());
    }
}