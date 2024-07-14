package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.availability.ApplicationAvailabilityAutoConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DbHelper;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.*;
import searchengine.repositories.*;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"SiteStatisticsRepository\" integration tests")
class SiteStatisticsRepositoryTest extends TestContainer {
    private final SiteStatisticsRepository siteStatisticsRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final EntityManager entityManager;
    private final NamedParameterJdbcTemplate jdbc;
    @Autowired
    private ApplicationAvailabilityAutoConfiguration applicationAvailabilityAutoConfiguration;

    @Test
    @DisplayName("Find all")
    public void testFindAll() {
        SiteEntity site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url("www.google.com")
                .name("Google").build();
        DbHelper.saveAndDetach(site, siteRepository, entityManager);

        SiteEntity site2 = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url("www.moogle.com")
                .name("Moogle").build();
        DbHelper.saveAndDetach(site2, siteRepository, entityManager);

        PageEntity page1 = PageEntity.builder()
                .site(site)
                .path("/")
                .code(200)
                .content("Hello world").build();
        DbHelper.saveAndDetach(page1, pageRepository, entityManager);

        PageEntity page2 = PageEntity.builder()
                .site(site)
                .path("/")
                .code(200)
                .content("Hello world").build();
        DbHelper.saveAndDetach(page2, pageRepository, entityManager);

        PageEntity page3 = PageEntity.builder()
                .site(site2)
                .path("/")
                .code(200)
                .content("Hello world").build();
        DbHelper.saveAndDetach(page3, pageRepository, entityManager);

        LemmaEntity lemma1 = LemmaEntity.builder()
                .site(site)
                .lemma("ягуар")
                .frequency(10)
                .build();
        DbHelper.saveAndDetach(lemma1, lemmaRepository, entityManager);

        LemmaEntity lemma2 = LemmaEntity.builder()
                .site(site2)
                .lemma("ягуар")
                .frequency(10)
                .build();
        DbHelper.saveAndDetach(lemma2, lemmaRepository, entityManager);

        List<SiteStatistics> statistics = siteStatisticsRepository.findAll();
        assertEquals(2, statistics.size());
        assertEquals(2, statistics.get(0).getPages());
    }
}