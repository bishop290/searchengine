package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.util.List;

@RequiredArgsConstructor
public class PagesManager {
    private final SiteEntity siteEntity;
    private final PageJsoupManager jsoupManager;
    private final PageEntitiesManager entitiesManager;

    private final PageLinksCache cache = new PageLinksCache();
    private boolean stopFlag = false;

    public void stop() {
        this.stopFlag = true;
    }

    public String domain() {
        return siteEntity.getUrl();
    }

    public void statusStop() {
        String text = "Индексация остановлена пользователем";
        entitiesManager.siteUpdate(siteEntity, Status.FAILED, text);
    }

    public void statusIndexed() {
        if (isStop()) {
            return;
        }
        entitiesManager.siteUpdate(siteEntity, Status.INDEXED, "");
    }

    public void statusFailed() {
        String text = String.format("%d: %s", jsoupManager.getCode(), jsoupManager.getBody());
        entitiesManager.siteUpdate(siteEntity, Status.FAILED, text);
    }

    public boolean parse(String url) {
        jsoupManager.connect(url);
        return jsoupManager.getDocument() != null && jsoupManager.getCode() != -1;
    }

    public void save() {
        entitiesManager.saveEntities();
    }

    public void save(String url) {
        entitiesManager.saveEntities(
                PageEntity.builder()
                        .path(jsoupManager.getPath(url, domain()))
                        .site(siteEntity)
                        .code(jsoupManager.getCode())
                        .content(jsoupManager.getBody())
                        .build());
    }

    public List<String> links() {
        return jsoupManager.getLinks(domain());
    }

    public boolean isNewUrl(String url) {
        return !cache.containsLink(url);
    }

    public boolean isStop() {
        return stopFlag;
    }
}
