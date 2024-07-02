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
import java.util.ArrayList;
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

        DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);
        SiteEntity savedSite = DatabaseWorker.get(SiteEntity.class, jdbc);

        assertEquals(savedSite.getStatus(), Status.INDEXING);
        assertTrue((savedSite.getStatusTime().getTime() - timestamp.getTime()) < permissibleTimeError);
        assertEquals(savedSite.getLastError(), lastError);
        assertEquals(savedSite.getUrl(), url);
        assertEquals(savedSite.getName(), name);
    }

    @Test
    @DisplayName("Remove the site with all dependencies")
    public void testDeleteAll() {
        SiteEntity site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url("www.google.com")
                .name("Google").build();

        PageEntity firstPage = PageEntity.builder()
                .site(site).path("www.google.com/hot-sausage-pie")
                .code(404).content("Hello world!").build();
        PageEntity secondPage = PageEntity.builder()
                .site(site).path("www.google.com/cat-care")
                .code(500).content("Привет мир!").build();

        LemmaEntity lemma = LemmaEntity.builder()
                .site(site)
                .lemma("lemma")
                .frequency(1)
                .build();

        IndexEntity index = IndexEntity.builder()
                .lemma(lemma)
                .page(firstPage)
                .rank(3)
                .build();

        DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);
        DatabaseWorker.saveAndDetach(firstPage, pageRepository, entityManager);
        DatabaseWorker.saveAndDetach(secondPage, pageRepository, entityManager);
        DatabaseWorker.saveAndDetach(lemma, lemmaRepository, entityManager);
        DatabaseWorker.saveAndDetach(index, indexRepository, entityManager);

        List<SiteEntity> entities = new ArrayList<>();
        entities.add(site);

        siteRepository.deleteAll(entities);
        siteRepository.flush();

        assertEquals(0, DatabaseWorker.count("page", jdbc));
        assertEquals(0, DatabaseWorker.count("lemma", jdbc));
        assertEquals(0, DatabaseWorker.count("`index`", jdbc));
        assertEquals(0, DatabaseWorker.count("site", jdbc));

        siteRepository.save(site);
        assertEquals(1, DatabaseWorker.count("site", jdbc));
        assertEquals(0, DatabaseWorker.count("page", jdbc));
        assertEquals(0, DatabaseWorker.count("lemma", jdbc));
        assertEquals(0, DatabaseWorker.count("`index`", jdbc));
    }

    @Test
    @DisplayName("Find sites by urls")
    public void testFindSitesByUrl() {
        int count = 10;
        int result = 3;
        List<String> urls = new ArrayList<>();
        urls.add("www.google.com5");
        urls.add("www.google.com6");
        urls.add("www.google.com7");
        urls.add("www.google.com256");

        for (int i = 0; i < count; i++) {
            SiteEntity site = SiteEntity.builder()
                    .status(Status.INDEXING)
                    .statusTime(new Timestamp(System.currentTimeMillis()))
                    .lastError("This is last error")
                    .url("www.google.com" + i)
                    .name("Google")
                    .build();
            DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);
        }

        List<SiteEntity> sites = siteRepository.findByUrlIn(urls);
        assertEquals(result, sites.size());
        System.out.println();
    }

}