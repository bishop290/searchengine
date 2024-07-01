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
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"PageRepository\" integration tests")
class PageRepositoryTest extends TestContainer {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final EntityManager entityManager;
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

        site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();
    }

    @Test
    @DisplayName("Save \"Page\" entity to db")
    public void testSaveToDb() {
        DatabaseWorker.saveToDb(site, siteRepository, entityManager);
        PageEntity page = PageEntity.builder()
                .site(site).path(path).code(code).content(content).build();

        DatabaseWorker.saveToDb(page, pageRepository, entityManager);
        PageEntity savedPage = DatabaseWorker.get(PageEntity.class, jdbc);

        assertEquals(savedPage.getPath(), path);
        assertEquals(savedPage.getCode(), code);
        assertEquals(savedPage.getContent(), content);
    }

    @Test
    @DisplayName("Find page by path")
    public void testFindByPath() {
        SiteEntity site2 = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl + "kkk")
                .name("Google").build();

        DatabaseWorker.saveToDb(site, siteRepository, entityManager);
        DatabaseWorker.saveToDb(site2, siteRepository, entityManager);
        PageEntity page1 = PageEntity.builder()
                .site(site).path(path).code(code).content(content).build();
        PageEntity page2 = PageEntity.builder()
                .site(site).path(path + "helloKitty").code(code).content(content).build();
        PageEntity page3 = PageEntity.builder()
                .site(site2).path(path).code(code).content(content).build();

        DatabaseWorker.saveToDb(page1, pageRepository, entityManager);
        DatabaseWorker.saveToDb(page2, pageRepository, entityManager);
        DatabaseWorker.saveToDb(page3, pageRepository, entityManager);

        PageEntity resultPage = pageRepository.findBySiteAndPath(site, path);
        assertEquals(resultPage.getPath(), path);
    }
}