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
    private final List<PageToDataTask> children = new ArrayList<>();
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
        List<LemmaEntity> lemmaEntities = manager.getLemmaEntities(lemmasInText); //можно вынести на уровень раньше
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
        startChildThreads(manager, indexEntities);
        manager.calculateRelativeRelevance();
    }

    private void startChildThreads(SearchManager manager, List<IndexEntity> indexes) {
        Integer pageId = indexes.get(0).getPage().getId();
        Integer currentId = pageId;

        List<IndexEntity> indexesForTask = new ArrayList<>();
        for (IndexEntity entity : indexes) {
            currentId = entity.getPage().getId();
            if (Objects.equals(currentId, pageId)) {
                indexesForTask.add(entity);
            } else {
                pageId = currentId;
                PageToDataTask task = new PageToDataTask(manager, indexesForTask);
                task.start();
                children.add(task);
                indexesForTask = new ArrayList<>();
            }
        }
        children.forEach(PageToDataTask::join);
    }
}
