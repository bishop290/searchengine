package searchengine.integration.components;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.components.PageToDbWorker;
import searchengine.integration.tools.DatabaseWorker;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"PageToDbWorker\" integration tests")
//class PageToDbWorkerTest extends TestContainer {
class PageToDbWorkerTest {
    private final NamedParameterJdbcTemplate namedJdbc;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final NamedParameterJdbcTemplate jdbc;
    private searchengine.components.PageToDbWorker worker;
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
        worker = new PageToDbWorker(siteRepository, lemmaRepository, pageRepository, indexRepository, jdbcTemplate);
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
        DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);
        worker.siteUpdate(site, Status.INDEXED, lastError);
        SiteEntity savedSite = DatabaseWorker.get(SiteEntity.class, namedJdbc);

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
        DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);

        PageEntity page = PageEntity.builder()
                .site(site)
                .path(path)
                .code(200)
                .content("Hello world").build();
        DatabaseWorker.saveAndDetach(page, pageRepository, entityManager);

        int frequency = 10;
        LemmaEntity lemma = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар")
                .frequency(frequency)
                .build();
        DatabaseWorker.saveAndDetach(lemma, lemmaRepository, entityManager);

        LemmaEntity lemma2 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар")
                .frequency(1)
                .build();
        DatabaseWorker.saveAndDetach(lemma2, lemmaRepository, entityManager);

        float rank = 0.1f;
        IndexEntity index = IndexEntity.builder()
                .page(page)
                .lemma(lemma)
                .rank(rank).build();
        DatabaseWorker.saveAndDetach(index, indexRepository, entityManager);

        IndexEntity index2 = IndexEntity.builder()
                .page(page)
                .lemma(lemma2)
                .rank(rank).build();
        DatabaseWorker.saveAndDetach(index2, indexRepository, entityManager);

        PageEntity newPage = pageRepository.findBySiteAndPath(site, path);

        worker.removePage(newPage);

        assertEquals(1, DatabaseWorker.count("site", jdbc));
        assertEquals(1, DatabaseWorker.count("lemma", jdbc));
        assertEquals(0, DatabaseWorker.count("page", jdbc));
        assertEquals(0, DatabaseWorker.count("index", jdbc));

        LemmaEntity siteLemma = DatabaseWorker.get(LemmaEntity.class, jdbc);
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
        DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);

        List<LemmaEntity> lemmas = new ArrayList<>();
        LemmaEntity lem1 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар1")
                .frequency(1)
                .build();
        DatabaseWorker.saveAndDetach(lem1, lemmaRepository, entityManager);

        LemmaEntity lem2 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар2")
                .frequency(1)
                .build();
        DatabaseWorker.saveAndDetach(lem2, lemmaRepository, entityManager);

        LemmaEntity lem3 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар3")
                .frequency(1)
                .build();
        DatabaseWorker.saveAndDetach(lem3, lemmaRepository, entityManager);

        LemmaEntity lem4 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар4")
                .frequency(1)
                .build();
        DatabaseWorker.saveAndDetach(lem4, lemmaRepository, entityManager);
        lem1.setFrequency(3);
        lem2.setFrequency(3);
        lem3.setFrequency(3);
        lem4.setFrequency(3);
        List<LemmaEntity> lem = new ArrayList<>();
        lem.add(lem1);
        lem.add(lem2);
        lem.add(lem3);
        lem.add(lem4);

        int[] res = worker.updateLemmas(lem);
        assertEquals(4, DatabaseWorker.count("lemma", jdbc));
    }

    @Test
    @DisplayName("Isert indexes to db")
    public void testInsertIndexesToDb() {
        int numberIndexesFromDb = 4;
        SiteEntity site = DatabaseWorker.newSiteEntityFromDb(siteRepository, entityManager);
        LemmaEntity lemma = DatabaseWorker.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        PageEntity page1 = DatabaseWorker.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        PageEntity page2 = DatabaseWorker.newPageEntityFromDb(site, "/hello/kitty", pageRepository, entityManager);
        PageEntity page3 = DatabaseWorker.newPageEntityFromDb(site, "/hello/frog", pageRepository, entityManager);
        PageEntity page4 = DatabaseWorker.newPageEntityFromDb(site, "/hello/pig", pageRepository, entityManager);
        List<IndexEntity> indexes = new ArrayList<>();
        indexes.add(DatabaseWorker.getIndexEntity(page1, lemma));
        indexes.add(DatabaseWorker.getIndexEntity(page2, lemma));
        indexes.add(DatabaseWorker.getIndexEntity(page3, lemma));
        indexes.add(DatabaseWorker.getIndexEntity(page4, lemma));

        worker.insertIndexes(indexes);
        assertEquals(numberIndexesFromDb, DatabaseWorker.count("index", jdbc));
    }
}