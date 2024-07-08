package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.config.Site;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Set;

public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    List<LemmaEntity> findBySiteAndLemmaIn(SiteEntity site, Set<String> lemmas);
    LemmaEntity findFirstBySiteUrlOrderByFrequencyDesc(String site);
    LemmaEntity findFirstByOrderByFrequencyDesc();
}
