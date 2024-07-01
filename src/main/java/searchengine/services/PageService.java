package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
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

    public synchronized PageEntity getPage(SiteEntity site, String path) {
        return pageRepository.findBySiteAndPath(site, path);
    }

    public synchronized void savePage(PageEntity page) {
        pageRepository.saveAndFlush(page);
    }

    public synchronized void removePage(PageEntity page) {
        List<LemmaEntity> lemmasForSave = new ArrayList<>();
        List<LemmaEntity> lemmasForRemove = new ArrayList<>();
        for (IndexEntity index : page.getIndexes()) {
            LemmaEntity lemma = index.getLemma();
            int newFrequency = lemma.getFrequency() - 1;
            if (newFrequency < 1) {
                lemmasForRemove.add(lemma);
            } else {
                lemma.setFrequency(newFrequency);
                lemmasForSave.add(lemma);
            }
        }
        lemmaRepository.deleteAll(lemmasForRemove);
        lemmaRepository.saveAll(lemmasForSave);
        lemmaRepository.flush();
        pageRepository.delete(page);
        pageRepository.flush();
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
}
