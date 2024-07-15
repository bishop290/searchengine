package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DbHelper;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"IndexRepository\" integration tests")
class IndexRepositoryTest extends TestContainer {
    private final IndexRepository indexRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final EntityManager entityManager;
    private final NamedParameterJdbcTemplate jdbc;

    @Test
    @DisplayName("Save \"Index\" entity to db")
    public void testSaveToDb() {
        float rank = 0.1f;
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        PageEntity page = DbHelper.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        LemmaEntity lemma = DbHelper.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page, lemma, indexRepository, entityManager);
        IndexEntity index = IndexEntity.builder()
                .page(page)
                .lemma(lemma)
                .rank(rank).build();
        DbHelper.saveAndDetach(index, indexRepository, entityManager);

        IndexEntity savedIndex = DbHelper.get(IndexEntity.class, jdbc);
        assertEquals(rank, savedIndex.getRank());
    }

    @Test
    @DisplayName("Find indexes by page")
    public void testFindByPage()  {
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        PageEntity page1 = DbHelper.newPageEntityFromDb(site, "/hello", pageRepository, entityManager);
        PageEntity page2 = DbHelper.newPageEntityFromDb(site, "/hello/kitty", pageRepository, entityManager);
        LemmaEntity lemma = DbHelper.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page1, lemma, indexRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page1, lemma, indexRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page2, lemma, indexRepository, entityManager);

        List<IndexEntity> resultEntities = indexRepository.findByPage(page1);
        assertEquals(2, resultEntities.size());
    }

    @Test
    @DisplayName("Find Indexes by pages and lemmas")
    public void findByPageInAndLemmaIn()  {
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
        DbHelper.newIndexEntityFromDb(page2, lemma2, indexRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page2, lemma3, indexRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page3, lemma, indexRepository, entityManager);
        DbHelper.newIndexEntityFromDb(page4, lemma3, indexRepository, entityManager);

        List<PageEntity> pages = new ArrayList<>();
        pages.add(page1);
        pages.add(page2);
        pages.add(page3);
        List<LemmaEntity> lemmas = new ArrayList<>();
        lemmas.add(lemma);
        lemmas.add(lemma2);

        List<IndexEntity> indexes = indexRepository.findByPageInAndLemmaInOrderByPageIdAsc(pages, lemmas);
        assertEquals(4, indexes.size());
        assertEquals("/hello", indexes.get(0).getPage().getPath());
    }
}