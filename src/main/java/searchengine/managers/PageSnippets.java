package searchengine.managers;

import lombok.Data;
import searchengine.dto.searching.Snippet;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageSnippets {
    private String site;
    private String siteName;
    private String uri;
    private String title;

    private float absoluteRelevance = 0;
    private List<String> lemmas = new ArrayList<>();
    private List<Snippet> snippets = new ArrayList<>();

    public void addLemmas(List<String> lemmas) {
        this.lemmas.addAll(lemmas);
    }

    public void addSnippet(String text) {
        snippets.add(createSnippet(String.format("...%s...<br><br>", setBold(text))));
    }

    public int snippetsSize() {
        return snippets.size();
    }

    public void setTitle(String title) {
        if (title.isEmpty()) {
            this.title = "заголовок отсутствует";
        } else {
            this.title = title;
        }
    }

    public void setAbsoluteRelevance(float rank) {
        absoluteRelevance += rank;
    }

    public void setRelativeRelevance(float maxRelevance) {
        snippets.forEach(snippet -> snippet.setRelevance(absoluteRelevance / maxRelevance));
    }

    private String setBold(String text) {
        for (String lemma : lemmas) {
            text = bold(lemma, text);
        }
        return text;
    }

     private String bold(String word, String text) {
        String boldWord = String.format("<b>%s</b>", word);
        return text.replaceAll(word, boldWord);
    }

    private Snippet createSnippet(String text) {
        return Snippet.builder()
                .site(site)
                .siteName(siteName)
                .uri(uri)
                .title(title)
                .snippet(text)
                .build();
    }
}
