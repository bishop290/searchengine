package searchengine.components;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.managers.Creator;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Database {
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final JdbcTemplate jdbcTemplate;

    public List<SiteEntity> sites() {
        return siteRepository.findAll();
    }

    public List<SiteEntity> sites(List<String> statuses) {
        return siteRepository.findByStatusIn(statuses);
    }

    public SiteEntity sites(Site site) {
        return siteRepository.findByUrl(site.getUrl());
    }

    public SiteEntity sites(String url) {
        return siteRepository.findByUrl(url);
    }


    public SiteEntity createSite(Site site) {
        SiteEntity siteEntity = Creator.site(site);
        siteRepository.save(siteEntity);
        siteRepository.flush();
        return siteEntity;
    }

    public void clearSites() {
        siteRepository.deleteAllInBatch();
    }

    public void siteUpdate(SiteEntity site, Status status, String lastError) {
        site.setStatusTime(new Timestamp(System.currentTimeMillis()));
        site.setStatus(status);
        site.setLastError(lastError);
        siteRepository.saveAndFlush(site);
    }

    public PageEntity pages(SiteEntity site, String path) {
        return pageRepository.findBySiteAndPath(site, path);
    }

    public Set<PageEntity> pages(SiteEntity site) {
        return pageRepository.findBySite(site);
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

    public Map<String, LemmaEntity> lemmas(SiteEntity site) {
        List<LemmaEntity> lemmas = lemmaRepository.findBySite(site);
        return lemmas.stream().collect(Collectors.toMap(LemmaEntity::getLemma, lemma -> lemma));
    }

    public Map<String, LemmaEntity> lemmas(SiteEntity site, Set<String> lemmaNames) {
        List<LemmaEntity> lemmas = lemmaRepository.findBySiteAndLemmaIn(site, lemmaNames);
        return lemmas.stream().collect(Collectors.toMap(LemmaEntity::getLemma, lemma -> lemma));
    }

    public void insertSites(List<SiteEntity> sites) {
        String query = """
                insert into `site` (`status`, `status_time`, `last_error`, `url`, `name`) 
                values(?,?,?,?,?)
                """;
        jdbcTemplate.batchUpdate(
                query,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, sites.get(i).getStatus().toString());
                        ps.setTimestamp(2, sites.get(i).getStatusTime());
                        ps.setString(3, sites.get(i).getLastError());
                        ps.setString(4, sites.get(i).getUrl());
                        ps.setString(5, sites.get(i).getName());
                    }

                    public int getBatchSize() {
                        return sites.size();
                    }
                });
    }

    public void insertPages(List<PageEntity> pages) {
        jdbcTemplate.batchUpdate(
                "insert into `page` (`site_id`, `path`, `code`, `content`) values(?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, pages.get(i).getSite().getId());
                        ps.setString(2, pages.get(i).getPath());
                        ps.setInt(3, pages.get(i).getCode());
                        ps.setString(4, pages.get(i).getContent());
                    }

                    public int getBatchSize() {
                        return pages.size();
                    }
                });
    }

    public void insertLemmas(List<LemmaEntity> lemmas) {
        jdbcTemplate.batchUpdate(
                "insert into `lemma` (`site_id`, `lemma`, `frequency`) values(?,?,?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, lemmas.get(i).getSite().getId());
                        ps.setString(2, lemmas.get(i).getLemma());
                        ps.setInt(3, lemmas.get(i).getFrequency());
                    }

                    public int getBatchSize() {
                        return lemmas.size();
                    }
                });
    }

    public void insertIndexes(List<IndexEntity> indexes) {
        jdbcTemplate.batchUpdate(
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

    public void updateLemmas(List<LemmaEntity> lemmas) {
        jdbcTemplate.batchUpdate(
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

    private void deletePage(PageEntity page) {
        pageRepository.delete(page);
        pageRepository.flush();
    }
}
