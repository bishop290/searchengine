package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.integration.tools.DbHelper;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatistics;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.repositories.SiteStatisticsRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"SiteStatisticsRepository\" integration tests")
class SiteStatisticsRepositoryTest extends TestContainer {
    private final SiteStatisticsRepository siteStatisticsRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final EntityManager entityManager;

    @Test
    @DisplayName("Find all")
    public void testFindAll() {
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        SiteEntity site2 = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        DbHelper.newPageEntityFromDb(site2, "/hello", pageRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site2, "злая белка", 1, lemmaRepository, entityManager);

        List<SiteStatistics> statistics = siteStatisticsRepository.findAll();
        assertEquals(2, statistics.size());
        assertEquals(2, statistics.get(0).getPages());
    }
}