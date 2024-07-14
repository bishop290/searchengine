package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@RequiredArgsConstructor
public class Storage {
    private final Set<String> links = ConcurrentHashMap.newKeySet();
    private final Map<String, LemmaEntity> lemmas = new ConcurrentHashMap<>();
    private final Set<PageEntity> pages = ConcurrentHashMap.newKeySet();
    private final Map<String, Map<String, Integer>> indexes = new ConcurrentHashMap<>();

    public boolean containsLink(String link) {
        boolean result = links.contains(link);
        links.add(link);
        return result;
    }

    public void addLemma(String key, SiteEntity site) {
        if (lemmas.containsKey(key)) {
            LemmaEntity entity = lemmas.get(key);
            entity.setFrequency(entity.getFrequency() + 1);
        } else {
            lemmas.put(key, Creator.lemma(site, key));
        }
    }

    public void addPage(PageEntity page, Map<String, Integer> index) {
        pages.add(page);
        indexes.put(page.getPath(), index);
    }

    public List<PageEntity> pages() { return new ArrayList<>(pages); }

    public LemmaEntity lemmas(String key) { return lemmas.get(key); }

    public List<LemmaEntity> lemmas() { return new ArrayList<>(lemmas.values()); }

    public Map<String, Integer> pageIndex(String path) { return indexes.get(path); }
}
