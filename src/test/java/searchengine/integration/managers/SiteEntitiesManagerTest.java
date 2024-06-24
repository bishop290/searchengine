package searchengine.integration.managers;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.config.Site;
import searchengine.integration.tools.DatabaseWorker;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.managers.SiteEntitiesManager;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"SiteEntityManager\" integration tests")
class SiteEntitiesManagerTest extends TestContainer {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final NamedParameterJdbcTemplate jdbc;
    private static List<Site> sites = new ArrayList<>();
    private SiteEntitiesManager manager;

    @BeforeAll
    public static void setSites() {
        int numberOfSites = 4;
        for (int i = 0; i < numberOfSites; i++) {
            sites.add(Site.builder().url("www.google.com" + i).name("Google" + i).build());
        }
    }

    @BeforeEach
    public void createManager() {
        manager = new SiteEntitiesManager(siteRepository, pageRepository);
    }

    @Test
    @DisplayName("Create \"SiteEntity\" entities.")
    public void testCreateEntities() {
        int numberOfEntities = 4;
        String urlOfTheFirstEntity = "www.google.com0";
        String nameOfTheSecondEntity = "Google0";

        manager.createEntities(sites);
        SiteEntity entity =  manager.getSiteEntities().get(0);

        assertEquals(numberOfEntities, manager.getSiteEntities().size());
        assertNull(entity.getId());
        assertEquals(urlOfTheFirstEntity, entity.getUrl());
        assertEquals(nameOfTheSecondEntity, entity.getName());
        assertEquals(Status.INDEXING, entity.getStatus());
        assertNotNull(entity.getStatusTime());
        assertNull(entity.getLastError());
    }

    @Test
    @DisplayName("Save entities to database.")
    public void testSaveToDatabase() {
        int numberOfRowsInDb = 4;

        manager.createEntities(sites);
        manager.saveToDatabase();

        assertEquals(numberOfRowsInDb, DatabaseWorker.count("site", jdbc));
    }
}