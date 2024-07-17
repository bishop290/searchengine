package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import searchengine.components.JsoupWorker;
import searchengine.components.LemmaSearch;
import searchengine.components.TextWorker;
import searchengine.dto.searching.PageData;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.PageRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SearchManager {
    private final SiteEntity site;
    private final LemmaSearch lemmaSearch;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final JsoupWorker jsoupWorker;
    private final TextWorker textWorker;
    @Getter
    private final List<PageData> data = new ArrayList<>();

    public String domain() {
        return site.getUrl();
    }

    public List<LemmaEntity> getLemmaEntities(Map<String, Integer> lemmas) {
        return lemmaSearch.getLemmaEntities(site, lemmas);
    }

    public List<PageEntity> findPagesForRareLemma(LemmaEntity lemma) {
        return pageRepository.findPagesByLemmaId(lemma.getId());
    }

    public List<IndexEntity> findIndexesSortingByPages(
            List<PageEntity> pages, List<LemmaEntity> lemmas) {
        return indexRepository.findByPageInAndLemmaInOrderByPageIdAsc(pages, lemmas);
    }

    public PageData collectPageData(PageData data, List<IndexEntity> indexes) {
        PageEntity page = indexes.get(0).getPage();
        data.setSite(site.getUrl());
        data.setSiteName(site.getName());
        data.setUri(page.getPath());

        PageText pageText = jsoupWorker.getTextFromHtml(page.getContent());
        if (pageText.title().isEmpty()) {
            data.setTitle("заголовок отсутствует");
        } else {
            data.setTitle(pageText.title());
        }
        return installSnippetsAndAbsoluteRelevance(data, indexes, pageText.body());
    }

    public void saveData(PageData data) {
        if (data != null) {
            this.data.add(data);
        }
    }

    private PageData installSnippetsAndAbsoluteRelevance(
            PageData data, List<IndexEntity> indexes, String text) {
        float absoluteRelevance = 0;
        List<String> allLemmas = new ArrayList<>();
        StringBuilder pattern = new StringBuilder();
        boolean delimiterFlag = false;

        pattern.append("(");
        for (IndexEntity index : indexes) {
            absoluteRelevance += index.getRank();

            String first = index.getLemma().getLemma();
            String second = textWorker.firstCharToUpperCase(first);
            String third = first.toUpperCase();
            allLemmas.addAll(Arrays.asList(first, second, third));

            appendToPattern(pattern, first, second, third, delimiterFlag);
            delimiterFlag = true;
        }
        pattern.append(")");

        String snippets = textWorker.snippets(text, pattern.toString());
        if (snippets.isEmpty()) {
            return null;
        }

        data.setRelevance(absoluteRelevance);
        data.setSnippet(setBold(snippets, allLemmas));
        return data;
    }

    private void appendToPattern(
            StringBuilder builder, String first, String second, String third, boolean flag) {
        if (flag) {
            builder.append("|");
        }
        builder.append(first).append("|");
        builder.append(second).append("|");
        builder.append(third);
    }

    private String setBold(String text, List<String> lemmas) {
        for (String lemma : lemmas) {
            text = textWorker.bold(lemma, text);
        }
        return text;
    }
}
