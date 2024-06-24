package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class PageEntitiesManager {
    private static final int LIMIT = 100;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SiteEntity site;
    private boolean completed = false;

    private final HashMap<String, PageEntity> entities = new HashMap<>();

    public String getDomain() {
        return site.getUrl();
    }

    public void saveEntities() {
        if (completed) {
            return;
        }
        List<PageEntity> pagesInDatabase = pageRepository.findBySiteAndPathIn(
                site, entities.keySet());

        for (PageEntity page : pagesInDatabase) {
            entities.remove(page.getPath());
        }
        pageRepository.saveAllAndFlush(entities.values());
        entities.clear();

        siteUpdate(Status.INDEXING, "");
    }

    @Synchronized
    public void addToDatabase(PageEntity pageEntity) {
        if (completed) {
            return;
        }
        if (entities.size() == LIMIT) {
            saveEntities();
        }
        pageEntity.setSite(site);
        entities.put(pageEntity.getPath(), pageEntity);
    }

    public void siteUpdate(Status status, String lastError) {
        if (completed) {
            return;
        }
        site.setStatusTime(new Timestamp(System.currentTimeMillis()));
        site.setStatus(status);
        site.setLastError(lastError);
        siteRepository.saveAndFlush(site);
    }

    public void completed() {
        this.completed = true;
    }
}
