package searchengine.managers;

import searchengine.config.Site;
import searchengine.model.*;

import java.sql.Timestamp;

public class Creator {
    public static SiteEntity site(Site site) {
        return SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError(null)
                .url(site.getUrl())
                .name(site.getName())
                .build();
    }

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
