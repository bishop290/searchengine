package searchengine.services;

import lombok.Getter;
import org.springframework.stereotype.Service;
import searchengine.config.EntitySettings;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.HashMap;

@Service
public class PageService {
    private static final int MINIMAL_LIMIT = 100;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    private final int limit;
    @Getter
    private final HashMap<String, PageEntity> entities;

    public PageService(EntitySettings entitySettings, SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.entities = new HashMap<>();
        this.limit = Math.max(entitySettings.getInsertLimit(), MINIMAL_LIMIT);
    }

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

    public synchronized void clearAll() {
        pageRepository.deleteAllInBatch();
    }
}
