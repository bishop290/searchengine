package searchengine.managers;

import searchengine.components.JsoupWorker;
import searchengine.components.PageToDbWorker;
import searchengine.components.TextWorker;
import searchengine.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PageManager {
    private final SiteEntity siteEntity;
    private final JsoupWorker jsoupService;
    private final PageToDbWorker dbWorker;
    private final TextWorker textWorker;
    private final Storage storage;
    private volatile boolean stopFlag;

    public PageManager(
            SiteEntity siteEntity, JsoupWorker jsoupService, PageToDbWorker dbWorker, TextWorker textWorker) {
        this.siteEntity = siteEntity;
        this.jsoupService = jsoupService;
        this.dbWorker = dbWorker;
        this.textWorker = textWorker;
        this.storage = new Storage(dbWorker);
        this.stopFlag = false;
    }

    public void stop() {
        this.stopFlag = true;
    }

    public boolean isStop() {
        return stopFlag;
    }

    public String domain() {
        return siteEntity.getUrl();
    }

    public boolean isDomain(String url) {
        return jsoupService.isDomain(url, siteEntity.getUrl());
    }

    public boolean initTextService() {
        try {
            textWorker.init();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void statusStop() {
        String text = "Индексация остановлена пользователем";
        dbWorker.siteUpdate(siteEntity, Status.FAILED, text);
    }

    public void statusIndexed() {
        if (stopFlag) {
            return;
        }
        dbWorker.siteUpdate(siteEntity, Status.INDEXED, "");
    }

    public void statusFailed(JsoupData data) {
        if (stopFlag) {
            return;
        }
        String text = String.format("%d: %s", data.code(), data.errorMessage());
        dbWorker.siteUpdate(siteEntity, Status.FAILED, text);
    }

    public JsoupData parse(String url) {
        return jsoupService.connect(url);
    }

    public Map<String, Integer> createLemmas(JsoupData data) {
        return textWorker.lemmas(data.document().text());
    }

    public List<String> links(JsoupData data) {
        return jsoupService.getLinks(data.document(), domain());
    }

    public boolean isNewUrl(String url) {
        return !storage.containsLink(url);
    }

    public void save(JsoupData data, Map<String, Integer> lemmas) {
        if (stopFlag) {
            return;
        }
        PageEntity page = EntityCreator.page(
                textWorker.path(data.url(), domain()), siteEntity, data.code(), data.document().html());
        dbWorker.savePage(page);

        synchronized (this) {
            List<LemmaEntity> lemmasFromDb = dbWorker.getLemmas(siteEntity, lemmas.keySet());
            storage.addLemmas(lemmasFromDb);

            List<LemmaEntity> lemmasForSave = new ArrayList<>();
            List<IndexEntity> indexesForSave = new ArrayList<>();

            for (Map.Entry<String, Integer> lemma : lemmas.entrySet()) {
                LemmaEntity currentLemma;
                if (storage.containsLemma(lemma.getKey())) {
                    currentLemma = storage.getLemma(lemma.getKey());
                } else {
                    currentLemma = EntityCreator.lemma(siteEntity, lemma.getKey());
                    lemmasForSave.add(currentLemma);
                }
                indexesForSave.add(EntityCreator.index(page, currentLemma, lemma.getValue()));
            }
            dbWorker.saveLemmas(lemmasForSave);
            storage.addLemmas(lemmasForSave);
            storage.addIndexes(indexesForSave);
        }
    }

    public void closeCache() {
        storage.close();
    }
}
