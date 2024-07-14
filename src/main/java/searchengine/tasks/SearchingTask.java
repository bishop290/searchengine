package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.dto.searching.PageData;
import searchengine.managers.SearchManager;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class SearchingTask implements Runnable {
    private final SearchManager manager;
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

    public List<PageData> data() {
        return manager.pageData();
    }

    @Override
    public void run() {
        List<LemmaEntity> lemmaEntities = manager.getLemmaEntities(lemmasInText);
        if (lemmaEntities.isEmpty() || lemmaEntities.size() < 2) {
            return;
        }
        List<PageEntity> pageEntities = manager.findPagesForRareLemma(lemmaEntities.get(0));
        if (pageEntities.isEmpty()) {
            return;
        }
        List<IndexEntity> indexEntities = manager.findIndexesSortingByPages(pageEntities, lemmaEntities);
        if (indexEntities.isEmpty()) {
            return;
        }
        startChildTasks(manager, indexEntities);
        manager.calculateRelativeRelevance();
    }

    private void startChildTasks(SearchManager manager, List<IndexEntity> indexes) {
        int pageId = indexes.get(0).getPage().getId();
        List<IndexEntity> indexesForTask = new ArrayList<>();

        for (IndexEntity index : indexes) {
            int currentId = index.getPage().getId();
            if (currentId == pageId) {
                indexesForTask.add(index);
            } else {
                startTask(manager, indexesForTask);
                pageId = currentId;
                indexesForTask = new ArrayList<>();
            }
        }
        if (!indexesForTask.isEmpty()) {
            startTask(manager, indexesForTask);
        }
        children.forEach(CollectTask::join);
    }

    private void startTask(SearchManager manager, List<IndexEntity> indexes) {
        CollectTask task = new CollectTask(manager, indexes);
        task.start();
        children.add(task);
    }
}
