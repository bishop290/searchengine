package searchengine.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.managers.Creator;
import searchengine.managers.PageManager;
import searchengine.managers.Storage;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.tasks.ParsingTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OnePageWorker {
    private final Database database;
    private final JsoupWorker jsoupWorker;
    private final TextWorker textWorker;

    public void parse(String url, Site site) {
        SiteEntity siteEntity = database.sites(site);
        if (siteEntity == null) {
            siteEntity = database.createSite(site);
            parseNewPage(url, siteEntity);
        } else {
            parseOldPage(url, siteEntity);
        }
    }

    public Site findDomain(String url, SitesList sites) {
        for (Site site : sites.getSites()) {
            String domain = site.getUrl().replaceFirst("^*/$", "");
            if (url.startsWith(domain + "/")) {
                return site;
            }
        }
        return null;
    }

    private void parseOldPage(String url, SiteEntity siteEntity) {
        String path = textWorker.path(url, siteEntity.getUrl());
        PageEntity pageEntity = database.pages(siteEntity, path);
        if (pageEntity == null) {
            parseNewPage(url, siteEntity);
        } else {
            database.removePage(pageEntity);
            parseNewPage(url, siteEntity);
        }
    }

    private void parseNewPage(String url, SiteEntity site) {
        Storage storage = new Storage();
        PageManager manager = new PageManager(site, jsoupWorker, database, textWorker, storage);
        ParsingTask task = new ParsingTask(url, manager);
        if (!task.parse()) {
            return;
        }
        task.calculateIndex();
        task.saveData();
        if (task.getPath().isEmpty()) {
            return;
        }
        manager.writePages();
        createLemmas(site, task);
        createIndexes(site, task);
        manager.statusIndexed();
        manager.stop();
    }

    private void createLemmas(SiteEntity site, ParsingTask task) {
        Map<String, LemmaEntity> lemmasFromDb = new HashMap<>();
        lemmasFromDb.putAll(database.lemmas(site, task.getIndex().keySet()));
        List<LemmaEntity> lemmasToUpdate = new ArrayList<>();
        List<LemmaEntity> lemmasToInsert = new ArrayList<>();
        task.getIndex().forEach((k, v) -> {
            if (lemmasFromDb.containsKey(k)) {
                LemmaEntity lemma = lemmasFromDb.get(k);
                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmasToUpdate.add(lemma);
            } else {
                lemmasToInsert.add(Creator.lemma(site, k));
            }
        });
        database.updateLemmas(lemmasToUpdate);
        database.insertLemmas(lemmasToInsert);
    }

    private void createIndexes(SiteEntity site, ParsingTask task) {
        PageEntity page = database.pages(site, task.getPath());
        Map<String, LemmaEntity> lemmasFromDb = database.lemmas(site, task.getIndex().keySet());
        List<IndexEntity> indexes = new ArrayList<>();
        task.getIndex().forEach((k, v) -> {
            indexes.add(Creator.index(page, lemmasFromDb.get(k), v));
        });
        database.insertIndexes(indexes);
    }
}
