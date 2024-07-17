package searchengine.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.comparators.LemmaComparator;
import searchengine.config.SearchSettings;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LemmaSearch {
    private final SearchSettings searchSettings;
    private final LemmaRepository lemmaRepository;

    public List<LemmaEntity> getLemmaEntities(SiteEntity site, Map<String, Integer> lemmas) {
        int limit = getFrequencyLimit(site);
        List<LemmaEntity> allEntities = lemmaRepository.findBySiteAndLemmaIn(site, lemmas.keySet());
        return deleteLemmasByLimit(allEntities, limit);
    }

    private List<LemmaEntity> deleteLemmasByLimit(List<LemmaEntity> entities, int limit) {
        return entities.stream()
                .filter(entity -> entity.getFrequency() < limit)
                .sorted(new LemmaComparator())
                .toList();
    }

    private int getFrequencyLimit(SiteEntity site) {
        LemmaEntity maxLemma = lemmaRepository.findFirstBySiteOrderByFrequencyDesc(site);
        return calculateFrequencyLimit(maxLemma, searchSettings.getFrequencyLimitInPercentage());
    }

    private int calculateFrequencyLimit(LemmaEntity maxLemma, int percentOfMaxFrequency) {
        int limit = percentOfMaxFrequency * maxLemma.getFrequency() / 100;
        return limit <= 0 ? 1 : limit;
    }
}
