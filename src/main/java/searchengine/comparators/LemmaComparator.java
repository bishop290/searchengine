package searchengine.comparators;

import searchengine.model.LemmaEntity;

public class LemmaComparator implements java.util.Comparator<LemmaEntity> {
    @Override
    public int compare(LemmaEntity o1, LemmaEntity o2) {
        return o1.getFrequency() - o2.getFrequency();
    }
}
