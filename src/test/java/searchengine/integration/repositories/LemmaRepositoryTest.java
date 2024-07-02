package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DatabaseWorker;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"LemmaRepository\" integration tests")
class LemmaRepositoryTest extends TestContainer {
    private final LemmaRepository repository;
    private final SiteRepository siteRepository;
    private final EntityManager entityManager;
    private final NamedParameterJdbcTemplate jdbc;

    private static String path;
    private static Integer code;
    private static String content;
    private static String siteUrl;
    private static SiteEntity site;
    @Autowired
    private LemmaRepository lemmaRepository;

    @BeforeAll
    public static void setSite() {
        path = "www.google.com/hot-sausage-pie";
        code = 404;
        content = "Hello world!";
        siteUrl = "www.google.com";
    }

    @Test
    @DisplayName("Save \"Lemma\" entity to db")
    public void testSaveToDb() {
        String lemma = "Это лемма";
        Integer freq = 10;

        site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();
        DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);

        LemmaEntity lemmaEntity = LemmaEntity.builder()
                .site(site)
                .lemma(lemma)
                .frequency(freq)
                .build();

        DatabaseWorker.saveAndDetach(lemmaEntity, repository, entityManager);
        LemmaEntity savedLemma = DatabaseWorker.get(LemmaEntity.class, jdbc);

        assertEquals(lemma, savedLemma.getLemma());
        assertEquals(freq, savedLemma.getFrequency());
    }

    @Test
    @DisplayName("Find entities by site and lemmas")
    public void testFindBySiteAndLemmas() {
        String lemma = "Это лемма-";
        Integer freq = 10;
        int numberOfLemmas = 10;
        Set<String> lemmaNames = new HashSet<>();
        lemmaNames.add(lemma + "2");
        lemmaNames.add(lemma + "5");
        lemmaNames.add(lemma + "0");
        lemmaNames.add(lemma + "255");
        int numbersOfFindLemmas = 3;

        site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();

        DatabaseWorker.saveAndDetach(site, siteRepository, entityManager);
        for (int i = 0; i < numberOfLemmas; i++) {
            DatabaseWorker.saveAndDetach(
                    LemmaEntity.builder()
                    .site(site)
                    .lemma(lemma + i)
                    .frequency(i)
                    .build(),
                    lemmaRepository,
                    entityManager);
        }

        List<LemmaEntity> lemmas = lemmaRepository.findBySiteAndLemmaIn(site, lemmaNames);
        assertEquals(numbersOfFindLemmas, lemmas.size());
    }
}