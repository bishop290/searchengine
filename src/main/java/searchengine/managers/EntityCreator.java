package searchengine.managers;

import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

public class EntityCreator {
    public static PageEntity page(String path, SiteEntity site, int code, String content) {
        return PageEntity.builder()
                .path(path)
                .site(site)
                .code(code)
                .content(content)
                .build();
    }

    public static LemmaEntity lemma(SiteEntity site, String lemma) {
        int defaultFrequency = 1;
        return LemmaEntity.builder()
                .site(site)
                .lemma(lemma)
                .frequency(defaultFrequency)
                .build();
    }

    public static IndexEntity index(PageEntity page, LemmaEntity lemma, float rank) {
        return IndexEntity.builder()
                .page(page)
                .lemma(lemma)
                .rank(rank)
                .build();
    }
}
