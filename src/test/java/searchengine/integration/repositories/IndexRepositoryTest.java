package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DatabaseWorker;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;

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
        SiteEntity site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url("www.google.com")
                .name("Google").build();
        DatabaseWorker.saveToDb(site, siteRepository, entityManager);

        PageEntity page = PageEntity.builder()
                .site(site)
                .path("/")
                .code(200)
                .content("Hello world").build();
        DatabaseWorker.saveToDb(page, pageRepository, entityManager);

        LemmaEntity lemma = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар")
                .frequency(10)
                .build();

        float rank = 0.1f;
        IndexEntity index = IndexEntity.builder()
                .page(page)
                .lemma(lemma)
                .rank(rank).build();

        DatabaseWorker.saveToDb(lemma, lemmaRepository, entityManager);
        DatabaseWorker.saveToDb(index, indexRepository, entityManager);

        IndexEntity savedIndex = DatabaseWorker.get(IndexEntity.class, jdbc);
        assertEquals(rank, savedIndex.getRank());
    }
}