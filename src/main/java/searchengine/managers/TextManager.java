package searchengine.managers;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class TextManager {
    private static LuceneMorphology rusMorphology;
    private static final Set<String> EXCEPTIONS = new HashSet<>(
            Arrays.asList("СОЮЗ", "МЕЖД", "ПРЕДЛ", "ЧАСТ"));

    public boolean init() {
        if (rusMorphology != null) {
            return true;
        }
        try {
            rusMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            return false;
        }
        return true;
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
