package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PageService {
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;

    public synchronized List<LemmaEntity> getLemmas(SiteEntity site, Set<String> names) {
        return lemmaRepository.findBySiteAndLemmaIn(site, names);
    }

    public synchronized void saveLemmas(List<LemmaEntity> lemmas) {
        lemmaRepository.saveAllAndFlush(lemmas);
    }

    public synchronized void savePage(PageEntity page) {
        pageRepository.saveAndFlush(page);
    }

    public synchronized void saveIndexes(List<IndexEntity> indexes) {
        indexRepository.saveAllAndFlush(indexes);
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
