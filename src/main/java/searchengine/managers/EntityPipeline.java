package searchengine.managers;

import lombok.RequiredArgsConstructor;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.JsoupService;
import searchengine.services.PageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class EntityPipeline {
    private final SiteEntity site;
    private final JsoupData data;
    private final PageService pageService;
    TextManager textManager;

    public void run() {
        textManager = new TextManager();
        textManager.init();
        List<IndexEntity> indexes = new ArrayList<>();
        PageEntity page = savePage();
        Map<String, Integer> lemmasInText = parseBody();
        saveLemmas(page, lemmasInText, indexes);
        pageService.saveIndexes(indexes);
    }

    private PageEntity savePage() {
        PageEntity page = createPage();
        pageService.savePage(page);
        return page;
    }

    private Map<String, Integer> parseBody() {
        return textManager.lemmas(data.document().body().text());
    }

    private synchronized void saveLemmas(PageEntity page, Map<String, Integer> lemmasInText, List<IndexEntity> indexes) {
        List<LemmaEntity> lemmas = pageService.getLemmas(site, lemmasInText.keySet());

        HashMap<String, LemmaEntity> lemmasFromDb = new HashMap<>();
        for (LemmaEntity lemma : lemmas) {
            lemma.setFrequency(lemma.getFrequency() + 1);
            lemmasFromDb.put(lemma.getLemma(), lemma);
        }

        List<LemmaEntity> lemmasForSave = new ArrayList<>();
        for (Map.Entry<String, Integer> lemma : lemmasInText.entrySet()) {
            LemmaEntity currentLemma;
            if (lemmasFromDb.containsKey(lemma.getKey())) {
                currentLemma = lemmasFromDb.get(lemma.getKey());
                lemmasForSave.add(currentLemma);
            } else {
                currentLemma = createLemma(site, lemma.getKey());
                lemmasForSave.add(currentLemma);
            }
            indexes.add(createIndex(page, currentLemma, lemma.getValue()));
        }
        pageService.saveLemmas(lemmasForSave);
    }

    private PageEntity createPage() {
        return PageEntity.builder()
                .path(textManager.path(data.url(), site.getUrl()))
                .site(site)
                .code(data.code())
                .content(data.document().body().html())
                .build();
    }

    private LemmaEntity createLemma(SiteEntity site, String lemma) {
        return LemmaEntity.builder()
                .site(site)
                .lemma(lemma)
                .frequency(1)
                .build();
    }

    private IndexEntity createIndex(PageEntity page, LemmaEntity lemma, float rank) {
        return IndexEntity.builder()
                .page(page)
                .lemma(lemma)
                .rank(rank)
                .build();
    }
}