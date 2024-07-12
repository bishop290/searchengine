package searchengine.comparators;

import searchengine.dto.searching.PageData;

public class PageDataComparator implements java.util.Comparator<PageData> {
    @Override
    public int compare(PageData o1, PageData o2) {
        return Float.compare(o2.getRelevance(), o1.getRelevance());
    }
}