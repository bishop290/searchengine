package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DatabaseWorker;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.Page;
import searchengine.model.Site;
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
    private final EntityManager manager;
    private final NamedParameterJdbcTemplate jdbc;

    @BeforeEach
    public void deleteAll() {
        siteRepository.deleteAll();
        pageRepository.deleteAll();
    }

    @Test
    @DisplayName("Save \"Page\" entity to db")
    public void testSaveToDb() {
        String path = "www.google.com/hot-sausage-pie";
        Integer code = 404;
        String content = "Hello world!";
        String siteUrl = "www.google.com";

        Site site = Site.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();

        Page page = Page.builder()
                .site(site).path(path).code(code).content(content).build();

        DatabaseWorker.saveToDb(site, siteRepository, manager);
        DatabaseWorker.saveToDb(page, pageRepository, manager);
        Page savedPage = DatabaseWorker.get(Page.class, jdbc);

        assertEquals(savedPage.getPath(), path);
        assertEquals(savedPage.getCode(), code);
        assertEquals(savedPage.getContent(), content);
    }
}