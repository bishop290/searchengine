package searchengine.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SiteToDbWorker {
    private final SiteRepository siteRepository;
    private final SitesList sites;

    public List<SiteEntity> createEntities() {
        if (sites.getSites().isEmpty()) {
            return new ArrayList<>();
        }
        return sites.getSites().stream().map(this::createSiteEntity).toList();
    }

    public void save(List<SiteEntity> entities) {
        siteRepository.saveAllAndFlush(entities);
    }

    public List<SiteEntity> sites() {
        return siteRepository.findAll();
    }

    public SiteEntity sites(Site site) {
        return siteRepository.findByUrl(site.getUrl());
    }

    public SiteEntity sites(String url) {
        Site site = findDomain(url);
        return siteRepository.findByUrl(site.getUrl());
    }

    public SiteEntity create(Site site) {
        SiteEntity siteEntity = createSiteEntity(site);
        siteRepository.save(siteEntity);
        siteRepository.flush();
        return siteEntity;
    }

    public void clearAll() {
        siteRepository.deleteAllInBatch();
        siteRepository.flush();
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
