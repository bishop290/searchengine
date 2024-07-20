package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DbHelper;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"SiteRepository\" integration tests")
class SiteRepositoryTest extends TestContainer {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
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

        DbHelper.saveAndDetach(site, siteRepository, entityManager);
        SiteEntity savedSite = DbHelper.get(SiteEntity.class, jdbc);

        assertEquals(savedSite.getStatus(), Status.INDEXING);
        assertTrue((savedSite.getStatusTime().getTime() - timestamp.getTime()) < permissibleTimeError);
        assertEquals(savedSite.getLastError(), lastError);
        assertEquals(savedSite.getUrl(), url);
        assertEquals(savedSite.getName(), name);
    }

    @Test
    @DisplayName("Remove the site with all dependencies")
    public void testDeleteAll() {
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        PageEntity page1 = DbHelper.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, "/hello/kitty", pageRepository, entityManager);
        LemmaEntity lemma = DbHelper.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page1, lemma, indexRepository, entityManager);

        List<SiteEntity> entities = new ArrayList<>();
        entities.add(site);

        siteRepository.deleteAll(entities);
        siteRepository.flush();

        assertEquals(0, DbHelper.count("page", jdbc));
        assertEquals(0, DbHelper.count("lemma", jdbc));
        assertEquals(0, DbHelper.count("index", jdbc));
        assertEquals(0, DbHelper.count("site", jdbc));

        siteRepository.save(site);
        assertEquals(1, DbHelper.count("site", jdbc));
        assertEquals(0, DbHelper.count("page", jdbc));
        assertEquals(0, DbHelper.count("lemma", jdbc));
        assertEquals(0, DbHelper.count("index", jdbc));
    }

    @Test
    @DisplayName("Find sites by statuses")
    public void testFindByStatusIn() {
        SiteEntity site1 = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("").url("a").name("a").build();

        SiteEntity site2 = SiteEntity.builder()
                .status(Status.FAILED)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("").url("a").name("a").build();

        SiteEntity site3 = SiteEntity.builder()
                .status(Status.INDEXED)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("").url("a").name("a").build();

        DbHelper.saveAndDetach(site1, siteRepository, entityManager);
        DbHelper.saveAndDetach(site2, siteRepository, entityManager);
        DbHelper.saveAndDetach(site3, siteRepository, entityManager);

        List<SiteEntity> sites = siteRepository.findByStatusIn(Arrays.asList("INDEXING", "INDEXED"));
        assertEquals(2, sites.size());
    }
}