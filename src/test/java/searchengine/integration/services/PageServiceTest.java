package searchengine.integration.services;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DatabaseWorker;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.PageService;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"EntityService\" integration tests")
class PageServiceTest extends TestContainer {
    private final NamedParameterJdbcTemplate namedJdbc;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final NamedParameterJdbcTemplate jdbc;
    private PageService manager;

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
        manager = new PageService(siteRepository, lemmaRepository, pageRepository, indexRepository);
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
        manager.siteUpdate(site, Status.INDEXED, lastError);
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
        manager.removePage(newPage);
        assertEquals(1, DatabaseWorker.count("site", jdbc));
        assertEquals(1, DatabaseWorker.count("lemma", jdbc));
        assertEquals(0, DatabaseWorker.count("page", jdbc));
        assertEquals(0, DatabaseWorker.count("`index`", jdbc));

        LemmaEntity siteLemma = DatabaseWorker.get(LemmaEntity.class, jdbc);
        assertEquals(frequency - 1, siteLemma.getFrequency());
    }

}