package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.components.*;
import searchengine.config.Site;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exceptions.IndexingIsAlreadyRunningException;
import searchengine.exceptions.IndexingIsNotRunningException;
import searchengine.exceptions.PageDoesNotBelongToTheListedSites;
import searchengine.managers.PageManager;
import searchengine.model.SiteEntity;
import searchengine.tasks.IndexingTask;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SiteToDbWorker siteWorker;
    private final PageToDbWorker pageWorker;
    private final JsoupWorker jsoupWorker;
    private final TextWorker textWorker;
    private final OnePageWorker onePageWorker;
    private final List<IndexingTask> tasks = new ArrayList<>();

    @Override
    public IndexingResponse start() {
        if (tasks.stream().anyMatch(IndexingTask::isRunning)) {
            throw new IndexingIsAlreadyRunningException();
        }

        siteWorker.clearAll();
        List<SiteEntity> sites = siteWorker.createEntities();
        siteWorker.save(sites);

        sites.forEach(site -> {
            PageManager manager = new PageManager(site, jsoupWorker, pageWorker, textWorker);
            IndexingTask task = new IndexingTask(manager);
            task.start();
            tasks.add(task);
        });
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
        Site site = siteWorker.findDomain(url);
        if (site == null) {
            throw new PageDoesNotBelongToTheListedSites();
        }
        onePageWorker.parse(url, site);
        return new IndexingResponse(true);
    }
}
