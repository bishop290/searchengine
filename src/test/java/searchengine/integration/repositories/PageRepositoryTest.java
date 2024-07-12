package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"PageRepository\" integration tests")
class PageRepositoryTest extends TestContainer {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final EntityManager entityManager;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final NamedParameterJdbcTemplate jdbc;

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

    @Test
    @DisplayName("Save \"Page\" entity to db")
    public void testSaveToDb() {
        site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();
        DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);
        PageEntity page = PageEntity.builder()
                .site(site).path(path).code(code).content(content).build();

        DatabaseWorker.saveAndDetach(page, pageRepository, entityManager);
        PageEntity savedPage = DatabaseWorker.get(PageEntity.class, jdbc);

        assertEquals(savedPage.getPath(), path);
        assertEquals(savedPage.getCode(), code);
        assertEquals(savedPage.getContent(), content);
    }

    @Test
    @DisplayName("Find page by site and path")
    public void testFindBySiteAndPath() {
        site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();

        SiteEntity site2 = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl + "kkk")
                .name("Google").build();

        DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);
        DatabaseWorker.saveAndDetach(site2, siteRepository, entityManager);

        PageEntity page1 = PageEntity.builder()
                .site(site).path(path).code(code).content(content).build();
        PageEntity page2 = PageEntity.builder()
                .site(site).path(path + "helloKitty").code(code).content(content).build();
        PageEntity page3 = PageEntity.builder()
                .site(site2).path(path).code(code).content(content).build();

        DatabaseWorker.saveAndDetach(page1, pageRepository, entityManager);
        DatabaseWorker.saveAndDetach(page2, pageRepository, entityManager);
        DatabaseWorker.saveAndDetach(page3, pageRepository, entityManager);


        PageEntity resultPage = pageRepository.findBySiteAndPath(site, path);
        assertEquals(resultPage.getPath(), path);
    }

    @Test
    @DisplayName("Find pages by lemma id")
    public void testFindPagesByLemmaId() {
        SiteEntity site = DatabaseWorker.newSiteEntityFromDb(siteRepository, entityManager);
        SiteEntity site2 = DatabaseWorker.newSiteEntityFromDb(siteRepository, entityManager);
        LemmaEntity lemma = DatabaseWorker.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        LemmaEntity lemma2 = DatabaseWorker.newLemmaEntityFromDb(site, "добрая белка", 1, lemmaRepository, entityManager);
        LemmaEntity lemma3 = DatabaseWorker.newLemmaEntityFromDb(site2, "нейтральная белка", 1, lemmaRepository, entityManager);

        PageEntity page1 = DatabaseWorker.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        PageEntity page2 = DatabaseWorker.newPageEntityFromDb(site, "/hello/kitty", pageRepository, entityManager);
        PageEntity page3 = DatabaseWorker.newPageEntityFromDb(site, "/hello/frog", pageRepository, entityManager);
        PageEntity page4 = DatabaseWorker.newPageEntityFromDb(site2, "/hello/pig", pageRepository, entityManager);

        DatabaseWorker.newIndexEntityFromDb(page1, lemma, indexRepository, entityManager);
        DatabaseWorker.newIndexEntityFromDb(page2, lemma, indexRepository, entityManager);
        DatabaseWorker.newIndexEntityFromDb(page3, lemma2, indexRepository, entityManager);
        DatabaseWorker.newIndexEntityFromDb(page4, lemma3, indexRepository, entityManager);

        List<PageEntity> pages = pageRepository.findPagesByLemmaId(lemma.getId());
        assertEquals(2, pages.size());
        assertEquals("/hello", pages.get(0).getPath());
        assertEquals("/hello/kitty", pages.get(1).getPath());
    }
}