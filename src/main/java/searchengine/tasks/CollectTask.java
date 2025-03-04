package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.managers.SearchManager;
import searchengine.model.IndexEntity;

import java.util.List;

@RequiredArgsConstructor
public class CollectTask implements Runnable {
    private final SearchManager manager;
    private final List<String> words;
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
        manager.saveData(manager.collectPageData(words, indexes));
    }
}
