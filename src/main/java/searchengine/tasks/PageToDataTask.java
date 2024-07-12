package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.dto.searching.PageData;
import searchengine.managers.SearchManager;
import searchengine.model.IndexEntity;

import java.util.List;

@RequiredArgsConstructor
public class PageToDataTask implements Runnable {
    private final SearchManager manager;
    private final List<IndexEntity> indexes;
    private Thread thread;

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        if (indexes.isEmpty()) {
            return;
        }
        PageData data = manager.collectPageData(new PageData(), indexes);
        manager.saveData(data);
    }
}
