package searchengine.components;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TextWorker {
    private static final List<String> EXCEPTIONS =
            Arrays.asList("СОЮЗ", "МЕЖД", "ПРЕДЛ", "ЧАСТ");
    private static LuceneMorphology rusMorphology;

    public void init() throws IOException {
        if (rusMorphology != null) {
            return;
        }
        rusMorphology = new RussianLuceneMorphology();
    }

    public Map<String, Integer> lemmas(String text) {
        HashMap<String, Integer> lemmas = new HashMap<>();
        String[] words = splitText(text);

        for (String word : words) {
            if (isValidWord(word)) {
                List<String> forms = rusMorphology.getNormalForms(word);
                if (!forms.isEmpty()) {
                    addAndCount(lemmas, forms.get(0));
                }
            }
        }
        return lemmas;
    }

    public String path(String url, String domain) {
        domain = domain.replaceFirst("^*(/)$", "");
        if (url.equals(domain) || url.equals(domain + "/")) {
            return "/";
        } else {
            return url.replaceFirst(domain, "");
        }
    }

    public String urlDecode(String url) {
        String useless = "url=";
        String head = "https://";
        url = url.replaceFirst(useless, "");
        url = URLDecoder.decode(url, StandardCharsets.UTF_8);
        return url.contains(head) ? url : head + url;
    }

    public String firstCharToUpperCase(String word) {
        if (word.isEmpty()) {
            return "";
        }
        char[] chars = word.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public String snippets(String text, String patternText) {
        List<String> snippets = breakTextOnSnippets(text);
        Pattern pattern = Pattern.compile(patternText);
        StringBuilder builder = new StringBuilder();

        snippets.forEach(snippet -> {
            Matcher matcher = pattern.matcher(snippet);
            if (matcher.find()) {
                appendSnippet(builder, snippet);
            }
        });
        return builder.toString();
    }

    public String bold(String word, String text) {
        String boldWord = String.format("<b>%s</b>", word);
        return text.replaceAll(word, boldWord);
    }

    private String[] splitText(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean isValidWord(String word) {
        if (word.isEmpty()) {
            return false;
        }
        List<String> forms;
        try {
            forms = rusMorphology.getMorphInfo(word);
        } catch (WrongCharaterException e) {
            return false;
        }
        if (forms.isEmpty()) {
            return false;
        }
        for (String form : forms) {
            for (String exception : EXCEPTIONS) {
                if (form.toUpperCase().contains(exception)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addAndCount(HashMap<String, Integer> map, String word) {
        int value = map.getOrDefault(word, 0);
        if (value == Integer.MAX_VALUE) {
            map.put(word, Integer.MAX_VALUE);
        } else {
            map.put(word, value + 1);
        }
    }

    private List<String> breakTextOnSnippets(String text) {
        int lengthOfSnippet = 300;
        text = text.replaceAll(System.lineSeparator(), "");

        List<String> snippets = new ArrayList<>();
        for (int i = 0; i < text.length(); i += lengthOfSnippet) {
            snippets.add(text.substring(i, Math.min(text.length(), i + lengthOfSnippet)));
        }
        return snippets;
    }

    private void appendSnippet(StringBuilder builder, String text) {
        builder.append("...").append(text).append("...").append(System.lineSeparator());
    }
}
