package searchengine.integration.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.integration.tools.DatabaseWorker;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;
import searchengine.services.WebsiteService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"WebsiteService\" integration tests")
class WebsiteServiceTest extends TestContainer {
    private final SiteRepository siteRepository;
    private final NamedParameterJdbcTemplate jdbc;
    private static SitesList sitesList;
    private WebsiteService service;

    @BeforeAll
    public static void setSites() {
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
        service = new WebsiteService(siteRepository, sitesList);
    }

    @Test
    @DisplayName("Create \"SiteEntity\" entities.")
    public void testCreateEntities() {
        int numberOfEntities = 4;
        String urlOfTheFirstEntity = "www.google.com0";
        String nameOfTheSecondEntity = "Google0";

        service.createEntities();
        SiteEntity entity = service.getSiteEntities().get(0);

        assertEquals(numberOfEntities, service.getSiteEntities().size());
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

        service.createEntities();
        service.saveToDatabase();

        assertEquals(numberOfRowsInDb, DatabaseWorker.count("site", jdbc));
    }
}