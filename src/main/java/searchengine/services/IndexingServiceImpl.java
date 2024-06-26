package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.EntitySettings;
import searchengine.config.JsoupSettings;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exceptions.IndexingIsAlreadyRunningException;
import searchengine.exceptions.IndexingIsNotRunningException;
import searchengine.managers.PageEntitiesManager;
import searchengine.managers.PageJsoupManager;
import searchengine.managers.PagesManager;
import searchengine.managers.SitesManager;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.tasks.IndexingTask;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sitesList;
    private final JsoupSettings jsoupSettings;
    private final EntitySettings entitySettings;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    private final List<IndexingTask> tasks = new ArrayList<>();

    @Override
    public IndexingResponse start() {
        if (tasks.stream().anyMatch(IndexingTask::isRunning)) {
            throw new IndexingIsAlreadyRunningException();
        }

        cleanIndexedData();

        SitesManager siteManager = new SitesManager(siteRepository);
        siteManager.createEntities(sitesList.getSites());
        siteManager.saveToDatabase();

        startIndexingProcesses(siteManager);

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

    private void startIndexingProcesses(SitesManager siteManager) {
        Connection connection = Jsoup.newSession()
                .userAgent(jsoupSettings.getAgent())
                .referrer(jsoupSettings.getReferrer());

        int minimalDelay = 1000;
        PageJsoupManager jsoupManager = new PageJsoupManager(
                connection,
                Math.max(jsoupSettings.getDelay(), minimalDelay));

        int minimalLimit = 100;
        PageEntitiesManager entitiesManager = new PageEntitiesManager(
                Math.max(entitySettings.getInsertLimit(), minimalLimit),
                siteRepository, pageRepository);

        siteManager.getSiteEntities().forEach(entity -> {
            IndexingTask task = new IndexingTask(
                    new PagesManager(entity, jsoupManager, entitiesManager));
            task.start();
            tasks.add(task);
        });
        tasks.forEach(IndexingTask::join);
    }

    private void cleanIndexedData() {
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
    }
}
