package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Set;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    String PAGE_BY_LEMMA_ID_QUERY = """
            select * from `page`
            where `id` in
            (select `page_id` from `index`
            where `lemma_id` = :id)
            """;

    Set<PageEntity> findBySite(SiteEntity site);
    PageEntity findBySiteAndPath(SiteEntity site, String path);

    @Query(value = PAGE_BY_LEMMA_ID_QUERY, nativeQuery = true)
    List<PageEntity> findPagesByLemmaId(Integer id);
}
