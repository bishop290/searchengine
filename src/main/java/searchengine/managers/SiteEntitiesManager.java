package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class SiteEntitiesManager {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final List<SiteEntity> siteEntities = new ArrayList<>();
    private final List<PageEntitiesManager> pageManagers = new ArrayList<>();

    public void createEntities(List<Site> sites) {
        if (sites.isEmpty()) {
            return;
        }
        for (Site site : sites) {
            SiteEntity siteEntity = SiteEntity.builder()
                    .status(Status.INDEXING)
                    .statusTime(new Timestamp(System.currentTimeMillis()))
                    .lastError(null)
                    .url(site.getUrl())
                    .name(site.getName())
                    .build();
            siteEntities.add(siteEntity);
            pageManagers.add(new PageEntitiesManager(siteRepository, pageRepository, siteEntity));
        }
    }

    public void saveToDatabase() {
        siteRepository.saveAllAndFlush(siteEntities);
    }
}
