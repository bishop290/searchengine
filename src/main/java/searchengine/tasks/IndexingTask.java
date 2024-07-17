package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.managers.PageManager;

import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
public class IndexingTask implements Runnable {
    private final PageManager manager;
    private final ForkJoinPool pool = new ForkJoinPool();

    public void start() {
        Thread thread = new Thread(this, manager.domain());
        thread.start();
    }

    public void stop() {
        if (!manager.isStop()) {
            manager.stop();
            manager.statusStop();
        }
    }

    public boolean isRunning() {
        return !manager.isStop();
    }

    @Override
    public void run() {
        pool.invoke(new ParsingTask(manager.domain(), manager));
        pool.shutdown();
        if (manager.isStop()) {
            return;
        }
        manager.writePages();
        manager.writeLemmas();
        manager.prepareStorage();
        manager.handleIndexes();
        manager.statusIndexed();
        manager.stop();
    }
}
