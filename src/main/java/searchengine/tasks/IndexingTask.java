package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import searchengine.config.JsoupSettings;
import searchengine.managers.LinksCache;
import searchengine.managers.PageEntitiesManager;
import searchengine.managers.PageJsoupManager;
import searchengine.model.Status;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class IndexingTask implements Runnable {
    private final PageEntitiesManager pageManager;
    private final JsoupSettings jsoupSettings;
    private ForkJoinPool pool;
    private Thread thread;

    public void start() {
        thread = new Thread(this, pageManager.getDomain());
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
        pool.shutdownNow();
        pageManager.siteUpdate(Status.FAILED, "Индексация остановлена пользователем");
        pageManager.completed();
    }

    @Override
    public void run() {
        Connection connection = Jsoup.newSession()
                .userAgent(jsoupSettings.getAgent()).referrer(jsoupSettings.getReferrer());
        PageJsoupManager jsoupManager = new PageJsoupManager(pageManager.getDomain(), connection, pageManager.getDomain());
        LinksCache linksCache = new LinksCache(500, 3);

        pool = new ForkJoinPool();
        pool.invoke(new PageParsingTask(jsoupManager, pageManager, linksCache));

        pageManager.saveEntities();
        pageManager.siteUpdate(Status.INDEXED, "");
        pageManager.completed();
    }
}
