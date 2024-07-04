package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.managers.MainPageManager;

import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
public class SiteIndexingTask implements Runnable {
    private final MainPageManager manager;

    private Thread thread;
    private final ForkJoinPool pool = new ForkJoinPool();

    public void start() {
        thread = new Thread(this, manager.domain());
        thread.start();
    }

    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        pool.invoke(new PageParsingTask(manager.domain(), manager));
        pool.shutdown();
        manager.closeCache();
        manager.statusIndexed();
        manager.stop();
    }
}
