package searchengine.integration.components;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DbHelper;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
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
    private searchengine.components.Database database;
    private final JdbcTemplate jdbcTemplate;

    private final EntityManager entityManager;

    private static String path;
    private static Integer code;
    private static String content;
    private static String siteUrl;
    private static SiteEntity site;

    @BeforeAll
    public static void setSite() {
        path = "www.google.com/hot-sausage-pie";
        code = 404;
        content = "Hello world!";
        siteUrl = "www.google.com";
    }

    @BeforeEach
    public void init() {
        database = new searchengine.components.Database(siteRepository, lemmaRepository, pageRepository, indexRepository, jdbcTemplate);
    }

    @Test
    @DisplayName("Site update")
    void testSiteUpdate() {
        String lastError = "";
        site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();
        DbHelper.saveAndDetach(site, siteRepository, entityManager);
        database.siteUpdate(site, Status.INDEXED, lastError);
        SiteEntity savedSite = DbHelper.get(SiteEntity.class, namedJdbc);

        assertEquals(Status.INDEXED, savedSite.getStatus());
        assertEquals(lastError, savedSite.getLastError());
        assertNotNull(savedSite.getStatusTime());
    }

    @Test
    @DisplayName("Delete Page")
    void testDeletePage() {
        SiteEntity site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();
        DbHelper.saveAndDetach(site, siteRepository, entityManager);

        PageEntity page = PageEntity.builder()
                .site(site)
                .path(path)
                .code(200)
                .content("Hello world").build();
        DbHelper.saveAndDetach(page, pageRepository, entityManager);

        int frequency = 10;
        LemmaEntity lemma = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар")
                .frequency(frequency)
                .build();
        DbHelper.saveAndDetach(lemma, lemmaRepository, entityManager);

        LemmaEntity lemma2 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар")
                .frequency(1)
                .build();
        DbHelper.saveAndDetach(lemma2, lemmaRepository, entityManager);

        float rank = 0.1f;
        IndexEntity index = IndexEntity.builder()
                .page(page)
                .lemma(lemma)
                .rank(rank).build();
        DbHelper.saveAndDetach(index, indexRepository, entityManager);

        IndexEntity index2 = IndexEntity.builder()
                .page(page)
                .lemma(lemma2)
                .rank(rank).build();
        DbHelper.saveAndDetach(index2, indexRepository, entityManager);

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
        SiteEntity site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();
        DbHelper.saveAndDetach(site, siteRepository, entityManager);

        List<LemmaEntity> lemmas = new ArrayList<>();
        LemmaEntity lem1 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар1")
                .frequency(1)
                .build();
        DbHelper.saveAndDetach(lem1, lemmaRepository, entityManager);

        LemmaEntity lem2 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар2")
                .frequency(1)
                .build();
        DbHelper.saveAndDetach(lem2, lemmaRepository, entityManager);

        LemmaEntity lem3 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар3")
                .frequency(1)
                .build();
        DbHelper.saveAndDetach(lem3, lemmaRepository, entityManager);

        LemmaEntity lem4 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар4")
                .frequency(1)
                .build();
        DbHelper.saveAndDetach(lem4, lemmaRepository, entityManager);
        lem1.setFrequency(3);
        lem2.setFrequency(3);
        lem3.setFrequency(3);
        lem4.setFrequency(3);
        List<LemmaEntity> lem = new ArrayList<>();
        lem.add(lem1);
        lem.add(lem2);
        lem.add(lem3);
        lem.add(lem4);

        //int[] res = database.updateLemmas(lem);
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