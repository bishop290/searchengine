package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.components.SiteToDbWorker;
import searchengine.components.TextWorker;
import searchengine.config.SearchSettings;
import searchengine.config.Site;
import searchengine.dto.searching.SearchRequest;
import searchengine.dto.searching.SearchResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final TextWorker textWorker;
    private final SiteToDbWorker siteWorker;
    private final LemmaRepository lemmaRepository;
    private final SearchSettings searchSettings;

    @Override
    public SearchResponse search(SearchRequest request) {
        Map<String, Integer> lemmas = getLemmas(request.query());
        List<LemmaEntity> lemmaEntities = getLemmaEntities(lemmas, request.site());
        return null;
    }

    private Map<String, Integer> getLemmas(String text) {
        try {
            textWorker.init();
        } catch (IOException e) {
            e.getMessage(); /* возврат исключения */
        }
        return textWorker.lemmas(text);
    }

    private List<LemmaEntity> getLemmaEntities(Map<String, Integer> lemmas, String site) {
        int limit = getFrequencyLimit(site);
        System.out.println();
        return null;
    }

    private int getFrequencyLimit(String site) {
        LemmaEntity maxLemma;
        if (site == null) {
            maxLemma = lemmaRepository.findFirstByOrderByFrequencyDesc();
        } else {
            Site domain = siteWorker.findDomain(site + "/");
            maxLemma = lemmaRepository.findFirstBySiteUrlOrderByFrequencyDesc(domain.getUrl());
        }
        int percentOfMaxFrequency = Math.max(searchSettings.getFrequencyLimitInPercentage(), 1);
        int limit = percentOfMaxFrequency * maxLemma.getFrequency() / 100;
        return limit <= 0 ? 1 : limit;
    }
}
