package searchengine.comparators;

import searchengine.managers.PageSnippets;

public class PageSnippetsComparator implements java.util.Comparator<PageSnippets> {
    @Override
    public int compare(PageSnippets o1, PageSnippets o2) {
        return Float.compare(o2.getAbsoluteRelevance(), o1.getAbsoluteRelevance());
    }
}