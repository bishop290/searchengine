package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.managers.LinksCache;
import searchengine.managers.PageEntitiesManager;
import searchengine.managers.PageJsoupManager;
import searchengine.model.PageEntity;
import searchengine.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class PageParsingTask extends RecursiveAction {
    private final static int DELAY = 1000;
    
    private final PageJsoupManager jsoupManager;
    private final PageEntitiesManager pageManager;
    private final LinksCache linksCache;

    @Override
    protected void compute() {
        if (linksCache.containsLink(jsoupManager.getUrl())) {
            return;
        }

        startDelay();
        jsoupManager.connect();

        String path = jsoupManager.getPath();

        if (path.equals("/") && jsoupManager.isError()) {
            String errorMassage = String.format("%d: %s",
                    jsoupManager.getCode(), jsoupManager.getBody());
            pageManager.siteUpdate(Status.FAILED, errorMassage);
            pageManager.completed();
            return;
        } else if (jsoupManager.getCode() == -1) {
            return;
        }

        PageEntity page = PageEntity.builder()
                .path(path)
                .code(jsoupManager.getCode())
                .content(jsoupManager.getBody()).build();

        pageManager.addToDatabase(page);
        forkForLinks(jsoupManager, pageManager, linksCache);
    }

    private void startDelay() {
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void forkForLinks(
            PageJsoupManager jsoupManager, PageEntitiesManager pageManager, LinksCache linksCache) {
        List<PageParsingTask> tasks = new ArrayList<>();

        for (String link : jsoupManager.getLinks()) {
            PageParsingTask newTask = new PageParsingTask(jsoupManager.getChild(link), pageManager, linksCache);
            tasks.add(newTask);
            newTask.fork();
        }
        for (PageParsingTask task : tasks) {
            task.join();
        }
    }
}
