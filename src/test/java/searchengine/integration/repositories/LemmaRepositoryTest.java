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
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.SiteRepository;

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
    private final LemmaRepository lemmaRepository;

    @Test
    @DisplayName("Save \"Lemma\" entity to db")
    public void testSaveToDb() {
        String lemma = "Это лемма";
        Integer freq = 10;

        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site, lemma, freq, lemmaRepository, entityManager);
        LemmaEntity savedLemma = DbHelper.get(LemmaEntity.class, jdbc);

        assertEquals(lemma, savedLemma.getLemma());
        assertEquals(freq, savedLemma.getFrequency());
    }

    @Test
    @DisplayName("Find entities by site and lemmas")
    public void testFindBySiteAndLemmas() {
        String lemma = "Это лемма-";
        int numberOfLemmas = 10;
        int numbersOfFindLemmas = 3;
        Set<String> lemmaNames = new HashSet<>();
        lemmaNames.add(lemma + "2");
        lemmaNames.add(lemma + "5");
        lemmaNames.add(lemma + "0");
        lemmaNames.add(lemma + "255");

        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        for (int i = 0; i < numberOfLemmas; i++) {
            DbHelper.newLemmaEntityFromDb(site, lemma + i, i, lemmaRepository, entityManager);
        }

        List<LemmaEntity> lemmas = lemmaRepository.findBySiteAndLemmaIn(site, lemmaNames);
        assertEquals(numbersOfFindLemmas, lemmas.size());
    }

    @Test
    @DisplayName("Find first by site url order by freq desc")
    void testFindFirstBySiteUrlOrderByFrequencyDesc() {
        SiteEntity site = DbHelper.newSiteEntityFromDb(siteRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site, "злая белка", 1, lemmaRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site, "добрая белка", 2, lemmaRepository, entityManager);
        DbHelper.newLemmaEntityFromDb(site, "нейтральная белка", 3, lemmaRepository, entityManager);

        LemmaEntity lemma = lemmaRepository.findFirstBySiteOrderByFrequencyDesc(site);
        assertEquals(3, lemma.getFrequency());
    }
}