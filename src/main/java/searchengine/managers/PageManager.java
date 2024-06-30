package searchengine.managers;

import lombok.RequiredArgsConstructor;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.JsoupService;
import searchengine.services.PageService;

import java.util.List;

@RequiredArgsConstructor
public class PageManager {
    private final SiteEntity siteEntity;
    private final JsoupService jsoupService;
    private final PageService pageService;
    private final LinksCache cache = new LinksCache();
    private boolean stopFlag = false;

    public void stop() {
        this.stopFlag = true;
    }

    public boolean isStop() {
        return stopFlag;
    }

    public String domain() {
        return siteEntity.getUrl();
    }

    public void statusStop() {
        String text = "Индексация остановлена пользователем";
        pageService.siteUpdate(siteEntity, Status.FAILED, text);
    }

    public void statusIndexed() {
        if (stopFlag) {
            return;
        }
        pageService.siteUpdate(siteEntity, Status.INDEXED, "");
    }

    public void statusFailed() {
        if (stopFlag) {
            return;
        }
        String text = String.format("%d: %s", jsoupService.getCode(), jsoupService.getErrorMessage());
        pageService.siteUpdate(siteEntity, Status.FAILED, text);
    }

    public boolean parse(String url) {
        jsoupService.connect(url);
        return jsoupService.getDocument() != null && jsoupService.getCode() != -1;
    }

    public List<String> links() {
        return jsoupService.getLinks(domain());
    }

    public boolean isNewUrl(String url) {
        return !cache.containsLink(url);
    }

    public void save(String url) {
        if (stopFlag) {
            return;
        }
        new EntityPipeline(siteEntity, url, jsoupService, pageService)
                .run();
    }
}
