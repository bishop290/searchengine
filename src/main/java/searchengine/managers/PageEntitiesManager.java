package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.HashMap;

@RequiredArgsConstructor
public class PageEntitiesManager {
    private final int limit;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    @Getter
    private final HashMap<String, PageEntity> entities = new HashMap<>();

    public synchronized void saveEntities() {
        pageRepository.saveAllAndFlush(entities.values());
        entities.clear();
    }

    public synchronized void saveEntities(PageEntity pageEntity) {
        if (entities.size() == limit) {
            saveEntities();
        }
        entities.put(pageEntity.getPath(), pageEntity);
    }

    public synchronized void siteUpdate(SiteEntity site, Status status, String lastError) {
        site.setStatusTime(new Timestamp(System.currentTimeMillis()));
        site.setStatus(status);
        site.setLastError(lastError);
        siteRepository.saveAndFlush(site);
    }
}
