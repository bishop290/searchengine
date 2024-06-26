package searchengine.managers;

import lombok.Synchronized;

import java.util.HashSet;
import java.util.Set;

public class PageLinksCache {
    private final Set<String> links;

    public PageLinksCache() {
        this.links = new HashSet<>();
    }

    @Synchronized
    public boolean containsLink(String link) {
        boolean result = links.contains(link);
        links.add(link);
        return result;
    }

    public int getSize() {
        return links.size();
    }
}
