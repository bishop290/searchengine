package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import searchengine.components.JsoupWorker;
import searchengine.components.LemmaSearch;
import searchengine.components.TextWorker;
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

    public PageSnippets collectPageData(List<String> words, List<IndexEntity> indexes) {
        PageSnippets pageSnippets = new PageSnippets();
        PageEntity page = indexes.get(0).getPage();
        pageSnippets.setSite(site.getUrl());
        pageSnippets.setSiteName(site.getName());
        pageSnippets.setUri(page.getPath());

        PageText pageText = jsoupWorker.getTextFromHtml(page.getContent());
        pageSnippets.setTitle(pageText.title());

        return installSnippetsAndAbsoluteRelevance(pageSnippets, words, indexes, pageText.body());
    }

    public void saveData(PageSnippets pageSnippets) {
        if (pageSnippets != null) {
            data.add(pageSnippets);
            count += pageSnippets.snippetsSize();
        }
    }

    private PageSnippets installSnippetsAndAbsoluteRelevance(
            PageSnippets pageSnippets, List<String> words, List<IndexEntity> indexes, String text) {
        StringBuilder pattern = new StringBuilder();
        boolean delimiterFlag = false;

        pattern.append("(");
        for (String word : words) {
            List<String> forms = getWordForms(word);
            pageSnippets.addLemmas(forms);
            appendToPattern(pattern, forms, delimiterFlag);
            delimiterFlag = true;
        }
        for (IndexEntity index : indexes) {
            pageSnippets.setAbsoluteRelevance(index.getRank());
            List<String> forms = getWordForms(index.getLemma().getLemma());
            pageSnippets.addLemmas(forms);
            appendToPattern(pattern, forms, delimiterFlag);
        }
        pattern.append(")");

        createSnippets(pageSnippets, text, pattern.toString());
        if (pageSnippets.getSnippets().isEmpty()) {
            return null;
        }
        return pageSnippets;
    }

    private List<String> getWordForms(String word) {
        return Arrays.asList(word, textWorker.firstCharToUpperCase(word), word.toUpperCase());
    }

    private void appendToPattern(StringBuilder builder, List<String> forms, boolean flag) {
        if (forms.size() < 3) {
            return;
        }
        if (flag) {
            builder.append("|");
        }
        builder.append(forms.get(0)).append("|");
        builder.append(forms.get(1)).append("|");
        builder.append(forms.get(2));
    }

    private void createSnippets(PageSnippets pageSnippets, String text, String patternText) {
        List<String> allSnippets = textWorker.breakTextToSnippets(text);
        Pattern pattern = Pattern.compile(patternText);

        for (String snippet : allSnippets) {
            Matcher matcher = pattern.matcher(snippet);
            if (matcher.find()) {
                pageSnippets.addSnippet(snippet);
            }
        }
    }
}
