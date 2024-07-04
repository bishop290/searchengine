package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.components.JsoupWorker;
import searchengine.components.PageToDbWorker;
import searchengine.components.SiteToDbWorker;
import searchengine.components.TextWorker;
import searchengine.config.Site;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exceptions.IndexingIsAlreadyRunningException;
import searchengine.exceptions.IndexingIsNotRunningException;
import searchengine.exceptions.PageDoesNotBelongToTheListedSites;
import searchengine.managers.MainPageManager;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.tasks.SiteIndexingTask;
import searchengine.tasks.PageParsingTask;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SiteToDbWorker websiteService;
    private final PageToDbWorker pageService;
    private final JsoupWorker jsoupService;
    private final TextWorker textService;
    private final List<SiteIndexingTask> tasks = new ArrayList<>();

    @Override
    public IndexingResponse start() {
        if (tasks.stream().anyMatch(SiteIndexingTask::isRunning)) {
            throw new IndexingIsAlreadyRunningException();
        }
        websiteService.clearAllByUrls();
        websiteService.createEntities();
        websiteService.saveToDatabase();
        createTasks(websiteService.getSiteEntities());

        return new IndexingResponse(true);
    }

    @Override
    public IndexingResponse stop() {
        if (tasks.stream().anyMatch(SiteIndexingTask::isRunning)) {
            tasks.forEach(SiteIndexingTask::stop);
            return new IndexingResponse(true);
        }
        throw new IndexingIsNotRunningException();
    }

    @Override
    public IndexingResponse startOnePage(String url) {
        Site site = websiteService.findDomain(url);
        if (site == null) {
            throw new PageDoesNotBelongToTheListedSites();
        }
        parseOnePage(url, site);
        return new IndexingResponse(true);
    }

    private void createTasks(List<SiteEntity> sites) {
        sites.forEach(entity -> {
            SiteIndexingTask task = new SiteIndexingTask(
                    new MainPageManager(entity, jsoupService, pageService, textService));
            task.start();
            tasks.add(task);
        });
    }

    public void parseOnePage(String url, Site site) {
        SiteEntity siteEntity = websiteService.getSite(site);
        if (siteEntity == null) {
            siteEntity = websiteService.createSite(site);
            parseNewPage(url, siteEntity);
        } else {
            parseOldPage(url, siteEntity);
        }
    }

    private void parseNewPage(String url, SiteEntity siteEntity) {
        MainPageManager manager = new MainPageManager(siteEntity, jsoupService, pageService, textService);
        PageParsingTask parsingTask = new PageParsingTask(url, manager);
        parsingTask.parse();
        parsingTask.getLemmas();
        parsingTask.saveData();
        manager.closeCache();
    }

    private void parseOldPage(String url, SiteEntity siteEntity) {
        String path = textService.path(url, siteEntity.getUrl());
        PageEntity pageEntity = pageService.getPage(siteEntity, path);
        if (pageEntity == null) {
            parseNewPage(url, siteEntity);
        } else {
            pageService.removePage(pageEntity);
            parseNewPage(url, siteEntity);
        }
    }
}
