package searchengine.integration.managers;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DatabaseWorker;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.managers.PageEntitiesManager;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"PageEntityManager\" integration tests")
class PageEntitiesManagerTest extends TestContainer {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final JdbcTemplate jdbc;
    private PageEntitiesManager manager;

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

        site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();
    }

    @BeforeEach
    public void init() {
        manager = new PageEntitiesManager(100, siteRepository, pageRepository);
        DatabaseWorker.saveToDb(site, siteRepository, entityManager);
    }

    @Test
    @DisplayName("Save entities to database")
    void testSaveEntities() {
        int counter = 101;
        int numberEntitiesInManager = 1;
        int numberEntitiesToDb = 100;
        String entityPath = path + "0";


        for (int i = 0; i < counter; i++) {
            manager.saveEntities(PageEntity.builder()
                    .site(site)
                    .path(path + i)
                    .code(code)
                    .content(content + i)
                    .build());
        }
        assertEquals(numberEntitiesInManager, manager.getEntities().size());

        List<PageEntity> entities = pageRepository.findAll();
        assertEquals(numberEntitiesToDb, entities.size());
        assertEquals(siteUrl, entities.get(0).getSite().getUrl());
    }

    @Test
    @DisplayName("Site update")
    void testSiteUpdate() {
        String lastError = "";

        manager.siteUpdate(site, Status.INDEXED, lastError);
        SiteEntity savedSite = DatabaseWorker.get(SiteEntity.class, namedJdbc);

        assertEquals(Status.INDEXED, savedSite.getStatus());
        assertEquals(lastError, savedSite.getLastError());
        assertNotNull(savedSite.getStatusTime());
    }
}