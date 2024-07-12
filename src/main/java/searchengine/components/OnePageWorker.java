package searchengine.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.managers.PageManager;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.tasks.PageParsingTask;

@Component
@RequiredArgsConstructor
public class OnePageWorker {
    private final SiteToDbWorker siteWorker;
    private final PageToDbWorker pageWorker;
    private final JsoupWorker jsoupWorker;
    private final TextWorker textWorker;

    public void parse(String url, Site site) {
        SiteEntity siteEntity = siteWorker.sites(site);
        if (siteEntity == null) {
            siteEntity = siteWorker.create(site);
            parseNewPage(url, siteEntity);
        } else {
            parseOldPage(url, siteEntity);
        }
    }

    private void parseNewPage(String url, SiteEntity siteEntity) {
        PageManager manager = new PageManager(siteEntity, jsoupWorker, pageWorker, textWorker);
        PageParsingTask parsingTask = new PageParsingTask(url, manager);
        if (!parsingTask.parse()) {
            return;
        }
        parsingTask.getLemmas();
        parsingTask.saveData();
        manager.closeCache();
        manager.statusIndexed();
        manager.stop();
    }

    private void parseOldPage(String url, SiteEntity siteEntity) {
        String path = textWorker.path(url, siteEntity.getUrl());
        PageEntity pageEntity = pageWorker.getPage(siteEntity, path);
        if (pageEntity == null) {
            parseNewPage(url, siteEntity);
        } else {
            pageWorker.removePage(pageEntity);
            parseNewPage(url, siteEntity);
        }
    }
}
