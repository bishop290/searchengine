package searchengine.components;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PageToDbWorker {
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final JdbcTemplate jdbcTemplate;

    public List<LemmaEntity> getLemmas(SiteEntity site, Set<String> names) {
        return lemmaRepository.findBySiteAndLemmaIn(site, names);
    }

    public void saveLemmas(List<LemmaEntity> lemmas) {
        lemmaRepository.saveAllAndFlush(lemmas);
    }

    public PageEntity getPage(SiteEntity site, String path) {
        return pageRepository.findBySiteAndPath(site, path);
    }

    public void savePage(PageEntity page) {
        pageRepository.saveAndFlush(page);
    }

    public void removePage(PageEntity page) {
        List<IndexEntity> indexes = indexRepository.findByPage(page);
        if (indexes == null || indexes.isEmpty()) {
            deletePage(page);
            return;
        }
        List<LemmaEntity> lemmasForSave = new ArrayList<>();
        List<LemmaEntity> lemmasForRemove = new ArrayList<>();
        for (IndexEntity index : indexes) {
            LemmaEntity lemma = index.getLemma();
            int newFrequency = lemma.getFrequency() - 1;
            if (newFrequency < 1) {
                lemmasForRemove.add(lemma);
            } else {
                lemma.setFrequency(newFrequency);
                lemmasForSave.add(lemma);
            }
        }
        lemmaRepository.deleteAllInBatch(lemmasForRemove);
        updateLemmas(lemmasForSave);
        deletePage(page);
    }

    public void siteUpdate(SiteEntity site, Status status, String lastError) {
        site.setStatusTime(new Timestamp(System.currentTimeMillis()));
        site.setStatus(status);
        site.setLastError(lastError);
        siteRepository.saveAndFlush(site);
    }

    public int[] updateLemmas(List<LemmaEntity> lemmas) {
        return jdbcTemplate.batchUpdate(
                "update `lemma` set `frequency` = ? where `id` = ?",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, lemmas.get(i).getFrequency());
                        ps.setInt(2, lemmas.get(i).getId());
                    }
                    public int getBatchSize() {
                        return lemmas.size();
                    }
                });
    }

    public int[] insertIndexes(List<IndexEntity> indexes) {
        return jdbcTemplate.batchUpdate(
                "insert into `index` (`page_id`, `lemma_id`, `rank`) values(?,?,?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, indexes.get(i).getPage().getId());
                        ps.setInt(2, indexes.get(i).getLemma().getId());
                        ps.setFloat(3, indexes.get(i).getRank());
                    }
                    public int getBatchSize() {
                        return indexes.size();
                    }
                });
    }

    private void deletePage(PageEntity page) {
        pageRepository.delete(page);
        pageRepository.flush();
    }
}
