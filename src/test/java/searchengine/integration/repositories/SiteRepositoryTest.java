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
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"SiteRepository\" integration tests")
class SiteRepositoryTest extends TestContainer {
    private final SiteRepository repository;
    private final EntityManager manager;
    private final NamedParameterJdbcTemplate jdbc;

    @BeforeEach
    public void deleteAll() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Save \"Site\" entity to db")
    public void testSaveToDb() {
        long permissibleTimeError = 1000;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String lastError = "This is last error";
        String url = "www.google.com";
        String name = "Google";

        Site site = Site.builder()
                .status(Status.INDEXING)
                .statusTime(timestamp)
                .lastError(lastError)
                .url(url)
                .name(name).build();

        DatabaseWorker.saveToDb(site, repository, manager);
        Site savedSite = DatabaseWorker.get(Site.class, jdbc);

        assertEquals(savedSite.getStatus(), Status.INDEXING);
        assertTrue((savedSite.getStatusTime().getTime() - timestamp.getTime()) < permissibleTimeError);
        assertEquals(savedSite.getLastError(), lastError);
        assertEquals(savedSite.getUrl(), url);
        assertEquals(savedSite.getName(), name);
    }
}