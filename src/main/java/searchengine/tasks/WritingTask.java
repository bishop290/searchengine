package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.managers.Creator;
import searchengine.managers.PageManager;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class WritingTask implements Runnable {
    private final PageManager manager;
    private final PageEntity page;
    private final Set<IndexEntity> indexes;
    private Thread thread;

    public void start() {
        thread = new Thread(this, page.getPath());
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
        Map<String, Integer> data = manager.indexData(page);
        for (String key : data.keySet()) {
            indexes.add(Creator.index(page, manager.lemma(key), data.get(key)));
        }
    }
}
