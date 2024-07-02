package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exceptions.IndexingIsAlreadyRunningException;
import searchengine.exceptions.IndexingIsNotRunningException;
import searchengine.exceptions.PageDoesNotBelongToTheListedSites;
import searchengine.managers.PageManager;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.tasks.IndexingTask;
import searchengine.tasks.ParsingTask;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final WebsiteService websiteService;
    private final PageService pageService;
    private final JsoupService jsoupService;
    private final TextService textService;
    private final List<IndexingTask> tasks = new ArrayList<>();

    @Override
    public IndexingResponse start() {
        if (tasks.stream().anyMatch(IndexingTask::isRunning)) {
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
        if (tasks.stream().anyMatch(IndexingTask::isRunning)) {
            tasks.forEach(IndexingTask::stop);
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
            IndexingTask task = new IndexingTask(
                    new PageManager(entity, jsoupService, pageService, textService));
            task.start();
            tasks.add(task);
        });
        tasks.forEach(IndexingTask::join);
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
         ParsingTask parsingTask = new ParsingTask(
                 url, new PageManager(siteEntity, jsoupService, pageService, textService));
         parsingTask.parse();
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
