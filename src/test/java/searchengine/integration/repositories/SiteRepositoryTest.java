package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"SiteRepository\" integration tests")
class SiteRepositoryTest extends TestContainer {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final EntityManager entityManager;
    private final NamedParameterJdbcTemplate jdbc;

    @Test
    @DisplayName("Save \"Site\" entity to db")
    public void testSaveToDb() {
        long permissibleTimeError = 1000;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String lastError = "This is last error";
        String url = "www.google.com";
        String name = "Google";

        SiteEntity site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(timestamp)
                .lastError(lastError)
                .url(url)
                .name(name).build();

        DatabaseWorker.saveToDb(site, siteRepository, entityManager);
        SiteEntity savedSite = DatabaseWorker.get(SiteEntity.class, jdbc);

        assertEquals(savedSite.getStatus(), Status.INDEXING);
        assertTrue((savedSite.getStatusTime().getTime() - timestamp.getTime()) < permissibleTimeError);
        assertEquals(savedSite.getLastError(), lastError);
        assertEquals(savedSite.getUrl(), url);
        assertEquals(savedSite.getName(), name);
    }

    @Test
    @DisplayName("Delete all sites together with pages from the table \"site\"")
    public void testDeleteAll() {
        SiteEntity site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url("www.google.com")
                .name("Google").build();

        PageEntity firstPage = PageEntity.builder()
                .site(site).path("www.google.com/hot-sausage-pie")
                .code(404).content("Hello world!").build();
        PageEntity secondPage = PageEntity.builder()
                .site(site).path("www.google.com/cat-care")
                .code(500).content("Привет мир!").build();

        DatabaseWorker.saveToDb(site, siteRepository, entityManager);
        DatabaseWorker.saveToDb(firstPage, pageRepository, entityManager);
        DatabaseWorker.saveToDb(secondPage, pageRepository, entityManager);

        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();

        assertEquals(0, DatabaseWorker.count("page", jdbc));
        assertEquals(0, DatabaseWorker.count("site", jdbc));
    }
}