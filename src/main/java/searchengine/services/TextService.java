package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class TextService {
    private static final Set<String> EXCEPTIONS = new HashSet<>(
            Arrays.asList("СОЮЗ", "МЕЖД", "ПРЕДЛ", "ЧАСТ"));
    private static LuceneMorphology rusMorphology;

    public void init() throws IOException {
        if (rusMorphology != null) {
            return;
        }
        rusMorphology = new RussianLuceneMorphology();
    }

    public Map<String, Integer> lemmas(String text) {
        HashMap<String, Integer> lemmas = new HashMap<>();

        String[] words = text.split("\\s+");
        for (String word : words) {
            word = word.replaceAll("[^А-Яа-я]+", "").toLowerCase(Locale.ROOT);
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

    private boolean isValidWord(String word) {
        if (word.isEmpty()) {
            return false;
        }

        List<String> info;
        try {
            info = rusMorphology.getMorphInfo(word);
        } catch (WrongCharaterException e) {
            return false;
        }
        if (info.isEmpty()) {
            return false;
        }

        for (String data : info) {
            String[] infoSubstrings = data.split(" ");
            String type = infoSubstrings[infoSubstrings.length - 1].strip();
            if (EXCEPTIONS.contains(type)) {
                return false;
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
}
