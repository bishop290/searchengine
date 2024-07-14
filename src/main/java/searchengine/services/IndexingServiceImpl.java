package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.components.*;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exceptions.IndexingIsAlreadyRunningException;
import searchengine.exceptions.IndexingIsNotRunningException;
import searchengine.exceptions.PageDoesNotBelongToTheListedSites;
import searchengine.managers.Creator;
import searchengine.managers.PageManager;
import searchengine.managers.Storage;
import searchengine.model.SiteEntity;
import searchengine.tasks.IndexingTask;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService { ;
    private final SitesList sites;
    private final Database database;
    private final JsoupWorker jsoupWorker;
    private final TextWorker textWorker;
    private final OnePageWorker onePageWorker;
    private final List<IndexingTask> tasks = new ArrayList<>();

    @Override
    public IndexingResponse start() {
        if (tasks.stream().anyMatch(IndexingTask::isRunning)) {
            throw new IndexingIsAlreadyRunningException();
        }
        database.clearSites();
        List<SiteEntity> newSites = sites.getSites().stream().map(Creator::site).toList();
        database.insertSites(newSites);

        for (SiteEntity site : database.sites()) {
            Storage storage = new Storage();
            PageManager manager = new PageManager(site, jsoupWorker, database, textWorker, storage);
            IndexingTask task = new IndexingTask(manager);
            task.start();
            tasks.add(task);
        }
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
        url = textWorker.urlDecode(url);
        Site site = onePageWorker.findDomain(url, sites);
        if (site == null) {
            throw new PageDoesNotBelongToTheListedSites();
        }
        onePageWorker.parse(url, site);
        return new IndexingResponse(true);
    }
}
