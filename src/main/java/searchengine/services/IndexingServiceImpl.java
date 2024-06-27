package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exceptions.IndexingIsAlreadyRunningException;
import searchengine.exceptions.IndexingIsNotRunningException;
import searchengine.managers.PagesManager;
import searchengine.model.SiteEntity;
import searchengine.tasks.IndexingTask;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final WebsiteService websiteService;
    private final PageService pageService;
    private final JsoupService jsoupService;
    private final List<IndexingTask> tasks = new ArrayList<>();

    @Override
    public IndexingResponse start() {
        if (tasks.stream().anyMatch(IndexingTask::isRunning)) {
            throw new IndexingIsAlreadyRunningException();
        }
        clearData();
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

    private void createTasks(List<SiteEntity> sites) {
        sites.forEach(entity -> {
            IndexingTask task = new IndexingTask(new PagesManager(entity, jsoupService, pageService));
            task.start();
            tasks.add(task);
        });
        tasks.forEach(IndexingTask::join);
    }

    private void clearData() {
        pageService.clearAll();
        websiteService.clearAll();
    }
}
