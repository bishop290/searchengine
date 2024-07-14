package searchengine.integration.tools;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.HashMap;

public class DbHelper {

    public static <T, S> void saveToDb(T entity, CrudRepository<T, S> repository, EntityManager manager) {
        repository.save(entity);
        manager.flush();
    }

    public static <T, S> void saveAndDetach(T entity, CrudRepository<T, S> repository, EntityManager manager) {
        repository.save(entity);
        manager.flush();
        manager.detach(entity);
    }

    public static <T> T get(Class<T> entity, NamedParameterJdbcTemplate jdbc) {
        final String tableName = entity.getAnnotation(Table.class).name();
        final String query = String.format("select * from %s order by id desc limit 1", tableName);
        return jdbc.queryForObject(query, new HashMap<>(),
                new BeanPropertyRowMapper<>(entity, false));
    }

    public static Integer count(String tableName, NamedParameterJdbcTemplate jdbc) {
        final String query = String.format("select count(*) from `%s`", tableName);
        return jdbc.getJdbcTemplate().queryForObject(query, Integer.class);
    }

    public static SiteEntity getSiteEntity() {
         return SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("last error")
                .url("www.google.com")
                .name("Google").build();
    }

    public static SiteEntity newSiteEntityFromDb(SiteRepository repo, EntityManager manager) {
        SiteEntity site = getSiteEntity();
        saveAndDetach(site, repo, manager);
        return site;
    }

    public static LemmaEntity getLemmaEntity(SiteEntity site, String name, int freq) {
        return LemmaEntity.builder()
                .site(site).lemma(name).frequency(freq).build();
    }

    public static LemmaEntity newLemmaEntityFromDb(SiteEntity site, String name, int freq, LemmaRepository repo, EntityManager manager) {
        LemmaEntity lemma = getLemmaEntity(site, name, freq);
        saveAndDetach(lemma, repo, manager);
        return lemma;
    }

    public static PageEntity getPageEntity(SiteEntity site, String name) {
        return PageEntity.builder()
                .site(site).path(name).code(200).content("content").build();
    }

    public static PageEntity newPageEntityFromDb(SiteEntity site, String name, PageRepository repo, EntityManager manager) {
        PageEntity page = getPageEntity(site, name);
        saveAndDetach(page, repo, manager);
        return page;
    }

    public static IndexEntity getIndexEntity(PageEntity page, LemmaEntity lemma) {
        return IndexEntity.builder()
                .page(page).lemma(lemma).rank(1).build();
    }

    public static IndexEntity newIndexEntityFromDb(PageEntity page, LemmaEntity lemma, IndexRepository repo, EntityManager manager) {
        IndexEntity index = getIndexEntity(page, lemma);
        saveAndDetach(index, repo, manager);
        return index;
    }
}
