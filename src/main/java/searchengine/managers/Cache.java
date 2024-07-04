package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import searchengine.components.PageToDbWorker;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@RequiredArgsConstructor
public class Cache {
    private final PageToDbWorker dbWorker;
    private final Set<String> links = ConcurrentHashMap.newKeySet();
    private final Map<String, LemmaEntity> lemmas = new ConcurrentHashMap<>();
    private final List<IndexEntity> indexes = new ArrayList<>();
    @Setter
    private int limit = 10000;

    public boolean containsLink(String link) {
        boolean result = links.contains(link);
        links.add(link);
        return result;
    }

    public boolean containsLemma(String lemmaName) {
        return lemmas.containsKey(lemmaName);
    }

    public LemmaEntity getLemma(String lemmaName) {
        return lemmas.get(lemmaName);
    }

    public void addLemmas(List<LemmaEntity> entities) {
        updateLemmas();
        entities.forEach(entity -> {
            if (lemmas.containsKey(entity.getLemma())) {
                LemmaEntity lemma = lemmas.get(entity.getLemma());
                lemma.setFrequency(lemma.getFrequency() + 1);
            } else {
                lemmas.put(entity.getLemma(), entity);
            }
        });
    }

    public void addIndexes(List<IndexEntity> entities) {
        updateIndexes();
        indexes.addAll(entities);
    }

    public int getSize() {
        return links.size();
    }

    public void close() {
        dbWorker.updateLemmas(new ArrayList<>(lemmas.values()));
        dbWorker.insertIndexes(indexes);
    }

    private void updateLemmas() {
        if (lemmas.size() >= limit) {
            dbWorker.updateLemmas(new ArrayList<>(lemmas.values()));
            lemmas.clear();
        }
    }

    private void updateIndexes() {
        if (indexes.size() >= limit) {
            dbWorker.insertIndexes(indexes);
            indexes.clear();
        }
    }
}
