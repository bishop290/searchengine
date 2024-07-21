package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.managers.PageSnippets;
import searchengine.managers.SearchManager;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SearchingTask implements Runnable {
    private final SearchManager manager;
    private final List<String> words;
    private final Map<String, Integer> lemmasInText;
    private final List<CollectTask> children = new ArrayList<>();
    private Thread thread;

    public void start() {
        thread = new Thread(this, manager.domain());
        thread.start();
    }

    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PageSnippets> data() { return manager.getData(); }

    public int count() { return manager.getCount(); }

    @Override
    public void run() {
        List<LemmaEntity> lemmaEntities = manager.getLemmaEntities(lemmasInText);
        if (lemmaEntities.isEmpty()) {
            return;
        }

        List<PageEntity> pageEntities = manager.findPagesForRareLemma(lemmaEntities.get(0));
        if (pageEntities.isEmpty()) {
            return;
        }

        List<IndexEntity> indexEntities = manager.findIndexesSortingByPages(
                pageEntities, lemmaEntities);
        if (indexEntities.isEmpty()) {
            return;
        }

        startChildTasks(manager, indexEntities);
    }

    private void startChildTasks(SearchManager manager, List<IndexEntity> indexes) {
        int pageId = indexes.get(0).getPage().getId();
        List<IndexEntity> indexesForTask = new ArrayList<>();

        for (IndexEntity index : indexes) {
            int currentId = index.getPage().getId();
            if (currentId != pageId) {
                startTask(manager, indexesForTask);
                pageId = currentId;
                indexesForTask = new ArrayList<>();
            }
            indexesForTask.add(index);
        }
        startTask(manager, indexesForTask);
        children.forEach(CollectTask::join);
    }

    private void startTask(SearchManager manager, List<IndexEntity> indexes) {
        if (indexes.isEmpty()) {
            return;
        }
        CollectTask task = new CollectTask(manager, words, indexes);
        task.start();
        children.add(task);
    }
}
