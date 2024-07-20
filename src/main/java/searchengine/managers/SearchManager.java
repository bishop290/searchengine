package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import searchengine.components.JsoupWorker;
import searchengine.components.LemmaSearch;
import searchengine.components.TextWorker;
import searchengine.dto.searching.Snippet;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class SearchManager {
    private final SiteEntity site;
    private final LemmaSearch lemmaSearch;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final JsoupWorker jsoupWorker;
    private final TextWorker textWorker;
    @Getter
    private final List<PageSnippets> data = new ArrayList<>();
    @Getter
    private int count = 0;

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

    public PageSnippets collectPageData(List<IndexEntity> indexes) {
        PageSnippets pageSnippets = new PageSnippets();
        PageEntity page = indexes.get(0).getPage();
        pageSnippets.setSite(site.getUrl());
        pageSnippets.setSiteName(site.getName());
        pageSnippets.setUri(page.getPath());

        PageText pageText = jsoupWorker.getTextFromHtml(page.getContent());
        pageSnippets.setTitle(pageText.title());

        return installSnippetsAndAbsoluteRelevance(pageSnippets, indexes, pageText.body());
    }

    public void saveData(PageSnippets pageSnippets) {
        if (pageSnippets != null) {
            data.add(pageSnippets);
            count += pageSnippets.snippetsSize();
        }
    }

    private PageSnippets installSnippetsAndAbsoluteRelevance(
            PageSnippets pageSnippets, List<IndexEntity> indexes, String text) {
        StringBuilder pattern = new StringBuilder();
        boolean delimiterFlag = false;

        pattern.append("(");
        for (IndexEntity index : indexes) {
            pageSnippets.setAbsoluteRelevance(index.getRank());

            String first = index.getLemma().getLemma();
            String second = textWorker.firstCharToUpperCase(first);
            String third = first.toUpperCase();
            pageSnippets.addLemmas(Arrays.asList(first, second, third));

            appendToPattern(pattern, first, second, third, delimiterFlag);
            delimiterFlag = true;
        }
        pattern.append(")");

        createSnippets(pageSnippets, text, pattern.toString());
        if (pageSnippets.getSnippets().isEmpty()) {
            return null;
        }
        return pageSnippets;
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

    public void createSnippets(PageSnippets pageSnippets, String text, String patternText) {
        List<String> allSnippets = textWorker.breakTextToSnippets(text);
        Pattern pattern = Pattern.compile(patternText);

        allSnippets.forEach(snippet -> {
            Matcher matcher = pattern.matcher(snippet);
            if (matcher.find()) {
                pageSnippets.addSnippet(snippet);
            }
        });
    }
}
