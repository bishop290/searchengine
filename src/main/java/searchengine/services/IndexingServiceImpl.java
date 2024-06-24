package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupSettings;
import searchengine.config.SitesList;
import searchengine.managers.PageEntitiesManager;
import searchengine.managers.SiteEntitiesManager;
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
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private List<IndexingTask> tasks;

    @Override
    public void start() {
        cleanIndexedData();
        SiteEntitiesManager siteManager = new SiteEntitiesManager(siteRepository, pageRepository);
        siteManager.createEntities(sitesList.getSites());
        siteManager.saveToDatabase();
        startIndexingProcesses(siteManager);
    }

    @Override
    public void stop() {
        tasks.forEach(IndexingTask::stop);
    }

    public void startIndexingProcesses(SiteEntitiesManager siteManager) {
        tasks = new ArrayList<>();
        for (PageEntitiesManager manager : siteManager.getPageManagers()) {
            IndexingTask task = new IndexingTask(manager, jsoupSettings);
            task.start();
            tasks.add(task);
        }
        tasks.forEach(IndexingTask::join);
    }

    private void cleanIndexedData() {
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
    }
}
