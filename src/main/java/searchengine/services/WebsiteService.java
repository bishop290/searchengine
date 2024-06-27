package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Service
public class WebsiteService {
    private final SiteRepository siteRepository;
    private final SitesList sites;
    private final List<SiteEntity> siteEntities;

    public WebsiteService(SiteRepository siteRepository, SitesList sites) {
        this.siteRepository = siteRepository;
        this.sites = sites;
        this.siteEntities = new ArrayList<>();
    }

    public void createEntities() {
        if (sites.getSites().isEmpty()) {
            return;
        }
        for (Site site : sites.getSites()) {
            SiteEntity siteEntity = SiteEntity.builder()
                    .status(Status.INDEXING)
                    .statusTime(new Timestamp(System.currentTimeMillis()))
                    .lastError(null)
                    .url(site.getUrl())
                    .name(site.getName())
                    .build();
            siteEntities.add(siteEntity);
        }
    }

    public void saveToDatabase() {
        siteRepository.saveAllAndFlush(siteEntities);
    }

    public void clearAll() {
        siteRepository.deleteAllInBatch();
    }
}
