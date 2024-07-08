package searchengine.components;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Component
public class SiteToDbWorker {
    private final SiteRepository siteRepository;
    private final SitesList sites;
    private final List<SiteEntity> siteEntities;

    public SiteToDbWorker(SiteRepository siteRepository, SitesList sites) {
        this.siteRepository = siteRepository;
        this.sites = sites;
        this.siteEntities = new ArrayList<>();
    }

    public void createEntities() {
        if (sites.getSites().isEmpty()) {
            return;
        }
        sites.getSites().forEach(site -> siteEntities.add(createSiteEntity(site)));
    }

    public void saveToDatabase() {
        siteRepository.saveAllAndFlush(siteEntities);
    }

    public SiteEntity getSite(Site site) {
        return siteRepository.findByUrl(site.getUrl());
    }

    public SiteEntity createSite(Site site) {
        SiteEntity siteEntity = createSiteEntity(site);
        siteRepository.save(siteEntity);
        siteRepository.flush();
        return siteEntity;
    }

    public void clearAll() {
        siteRepository.deleteAll(siteEntities);
        siteRepository.flush();
        siteEntities.clear();
    }

    public Site findDomain(String url) {
        for (Site site : sites.getSites()) {
            String domain = site.getUrl().replaceFirst("^*/$", "");
            if (url.startsWith(domain + "/")) {
                return site;
            }
        }
        return null;
    }

    private SiteEntity createSiteEntity(Site site) {
        return SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError(null)
                .url(site.getUrl())
                .name(site.getName())
                .build();
    }
}
