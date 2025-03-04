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
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"PageRepository\" integration tests")
class PageRepositoryTest extends TestContainer {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final EntityManager entityManager;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final NamedParameterJdbcTemplate jdbc;

    @Test
    @DisplayName("Save \"Page\" entity to db")
    public void testSaveToDb() {
        String path = "www.google.com/hot-sausage-pie";
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, path, pageRepository, entityManager);

        PageEntity savedPage = DbHelper.get(PageEntity.class, jdbc);
        assertEquals(path, savedPage.getPath());
    }

    @Test
    @DisplayName("Find page by site and path")
    public void testFindBySiteAndPath() {
        String path = "/hello/kitty";
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        SiteEntity site2 = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        DbHelper.newPageEntityFromDb(site2, path, pageRepository, entityManager);
        DbHelper.newPageEntityFromDb(site, "/hello/frog", pageRepository, entityManager);

        PageEntity resultPage = pageRepository.findBySiteAndPath(site2, path);
        assertEquals(path, resultPage.getPath());
    }

    @Test
    @DisplayName("Find pages by lemma id")
    public void testFindPagesByLemmaId() {
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        SiteEntity site2 = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        LemmaEntity lemma = DbHelper.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        LemmaEntity lemma2 = DbHelper.newLemmaEntityFromDb(site, "добрая белка", 1, lemmaRepository, entityManager);
        LemmaEntity lemma3 = DbHelper.newLemmaEntityFromDb(site2, "нейтральная белка", 1, lemmaRepository, entityManager);

        PageEntity page1 = DbHelper.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        PageEntity page2 = DbHelper.newPageEntityFromDb(site, "/hello/kitty", pageRepository, entityManager);
        PageEntity page3 = DbHelper.newPageEntityFromDb(site, "/hello/frog", pageRepository, entityManager);
        PageEntity page4 = DbHelper.newPageEntityFromDb(site2, "/hello/pig", pageRepository, entityManager);

        DbHelper.newIndexEntityFromDb(page1, lemma, indexRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page2, lemma, indexRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page3, lemma2, indexRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page4, lemma3, indexRepository, entityManager);

        List<PageEntity> pages = pageRepository.findPagesByLemmaId(lemma.getId());
        assertEquals(2, pages.size());
        assertEquals("/hello", pages.get(0).getPath());
        assertEquals("/hello/kitty", pages.get(1).getPath());
    }
}