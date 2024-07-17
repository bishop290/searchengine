package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.List;

public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {
    List<IndexEntity> findByPage(PageEntity page);
    List<IndexEntity> findByPageInAndLemmaInOrderByPageIdAsc(
            List<PageEntity> pages, List<LemmaEntity> lemmas);
}
