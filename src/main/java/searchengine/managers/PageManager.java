package searchengine.managers;

import lombok.RequiredArgsConstructor;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.JsoupService;
import searchengine.services.PageService;
import searchengine.services.TextService;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class PageManager {
    private final SiteEntity siteEntity;
    private final JsoupService jsoupService;
    private final PageService pageService;
    private final TextService textService;
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

    public boolean isDomain(String url) {
        return jsoupService.isDomain(url, siteEntity.getUrl());
    }

    public boolean initTextService() {
        try {
            textService.init();
        } catch (IOException e) {
            return false;
        }
        return true;
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

    public void statusFailed(JsoupData data) {
        if (stopFlag) {
            return;
        }
        String text = String.format("%d: %s", data.code(), data.errorMessage());
        pageService.siteUpdate(siteEntity, Status.FAILED, text);
    }

    public JsoupData parse(String url) {
        return jsoupService.connect(url);
    }

    public List<String> links(JsoupData data) {
        return jsoupService.getLinks(data.document(), domain());
    }

    public boolean isNewUrl(String url) {
        return !cache.containsLink(url);
    }

    public void save(JsoupData data) {
        if (stopFlag) {
            return;
        }
        new EntityPipeline(siteEntity, data, pageService, textService)
                .run();
    }
}
