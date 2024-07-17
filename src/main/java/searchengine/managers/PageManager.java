package searchengine.managers;

import lombok.RequiredArgsConstructor;
import searchengine.components.Database;
import searchengine.components.JsoupWorker;
import searchengine.components.TextWorker;
import searchengine.config.IndexingSettings;
import searchengine.exceptions.IndexingTextWorkerException;
import searchengine.model.*;
import searchengine.tasks.WritingTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PageManager {
    private final IndexingSettings settings;
    private final SiteEntity site;
    private final JsoupWorker jsoupWorker;
    private final Database database;
    private final TextWorker textWorker;
    private final Storage storage = new Storage();

    private volatile boolean stopFlag = false;

    public void stop() { this.stopFlag = true; }

    public boolean isStop() { return stopFlag; }

    public String domain() { return site.getUrl(); }

    public boolean isDomain(String url) {
        return jsoupWorker.isDomain(url, site.getUrl());
    }

    public boolean initTextService() {
        try {
            textWorker.init();
        } catch (IOException e) {
            throw new IndexingTextWorkerException();
        }
        return true;
    }

    public void statusStop() {
        String text = "Индексация остановлена пользователем";
        database.siteUpdate(site, Status.FAILED, text);
    }

    public void statusIndexed() {
        if (stopFlag) {
            return;
        }
        database.siteUpdate(site, Status.INDEXED, "");
    }

    public void statusFailed(JsoupData data) {
        if (stopFlag) {
            return;
        }
        String text = String.format("%d: %s", data.code(), data.errorMessage());
        database.siteUpdate(site, Status.FAILED, text);
    }

    public JsoupData parse(String url) { return jsoupWorker.connect(url); }

    public Map<String, Integer> createIndex(JsoupData data) {
        return textWorker.lemmas(data.document().text());
    }

    public List<String> links(JsoupData data) {
        return jsoupWorker.getLinks(data.document(), domain());
    }

    public boolean isNewUrl(String url) { return !storage.containsLink(url); }

    public String save(JsoupData data, Map<String, Integer> index) {
        if (stopFlag) {
            return "";
        }
        String path = textWorker.path(data.url(), domain());
        String html = data.document().html();
        storage.addPage(Creator.page(path, site, data.code(), html), index);
        index.forEach((k, v) -> storage.addLemma(k, site));
        return path;
    }

    public void writePages() { database.insertPages(storage.pages()); }

    public void writeLemmas() { database.insertLemmas(storage.lemmas()); }

    public void writeIndexes(Set<IndexEntity> indexes) {
        database.insertIndexes(new ArrayList<>(indexes));
    }

    public void prepareStorage() {
        storage.getLinks().clear();
        storage.getPages().clear();
        storage.getPages().addAll(database.pages(site));
        storage.getLemmas().putAll(database.lemmas(site));
    }

    public Set<PageEntity> pages() { return storage.getPages(); }

    public LemmaEntity lemma(String name) { return storage.lemmas(name); }

    public Map<String, Integer> indexData(PageEntity page) {
        return storage.pageIndex(page.getPath());
    }

    public void handleIndexes() {
        List<List<PageEntity>> pageSets = new ArrayList<>();
        List<PageEntity> currentSet = new ArrayList<>();
        int counter = 0;

        for (PageEntity page : pages()) {
            if (counter >= settings.getNumberOfPagesToIndexAtOneTime()) {
                pageSets.add(currentSet);
                currentSet = new ArrayList<>();
                counter = 0;
            }
            currentSet.add(page);
            counter++;
        }
        if (!currentSet.isEmpty()) {
            pageSets.add(currentSet);
        }

        for (List<PageEntity> set : pageSets) {
            writeIndexes(set);
        }
    }

    private void writeIndexes(List<PageEntity> pages) {
        Set<IndexEntity> indexes = ConcurrentHashMap.newKeySet();
        List<WritingTask> children = new ArrayList<>();
        for (PageEntity page : pages) {
            WritingTask task = new WritingTask(this, page, indexes);
            task.start();
            children.add(task);
        }
        children.forEach(WritingTask::join);
        writeIndexes(indexes);
    }
}