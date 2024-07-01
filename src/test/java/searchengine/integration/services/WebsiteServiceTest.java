package searchengine.integration.services;

import jakarta.persistence.EntityManager;
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

import java.sql.Timestamp;
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
    private final EntityManager entityManager;

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

    @Test
    @DisplayName("Get possible urls")
    public void testPossibleUrls() {
        int result = 4;
        List<Site> testSites = new ArrayList<>();
        testSites.add(Site.builder().url("www.google.com").build());
        testSites.add(Site.builder().url("www.duckduckgo.com/").build());
        SitesList sites = new SitesList();
        sites.setSites(testSites);
        service = new WebsiteService(siteRepository, sites);

        List<String> possibleUrls = service.getPossibleUrls();
        assertEquals(result, possibleUrls.size());
    }

    @Test
    @DisplayName("Find domain by Url")
    public void testFindDomain() {
        String url1 = "https://duckduckgo.com/hellokitty";
        String url2 = "https://opennet.com/hellokitty";
        List<Site> testSites = new ArrayList<>();
        testSites.add(Site.builder().url("https://google.com").build());
        testSites.add(Site.builder().url("https://duckduckgo.com/").build());
        SitesList sites = new SitesList();
        sites.setSites(testSites);
        service = new WebsiteService(siteRepository, sites);

        assertEquals(testSites.get(1).getUrl(), service.findDomain(url1).getUrl());
        assertNull(service.findDomain(url2));
    }

    @Test
    @DisplayName("Get site entity")
    public void testGetSiteEntity() {
        String resultName = "google";
        Site site = Site.builder().url("https://google.com").name("").build();
        Site site2 = Site.builder().url("https://duck.com").name("").build();
        SiteEntity siteEntity = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(site.getUrl())
                .name("google")
                .build();
        DatabaseWorker.saveToDb(siteEntity, siteRepository, entityManager);
        assertEquals(resultName, service.getSite(site).getName());
        assertNull(service.getSite(site2));

    }

    @Test
    @DisplayName("Create site entity")
    public void testCreateSiteEntity() {
        String resultName = "hvahva";
        Site site = Site.builder().url("https://google.com").name(resultName).build();
        SiteEntity result = service.createSite(site);
        assertEquals(resultName, result.getName());
    }
}