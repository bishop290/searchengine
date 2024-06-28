package searchengine.managers;

import java.util.HashSet;
import java.util.Set;

public class LinksCache {
    private final Set<String> links;

    public LinksCache() {
        this.links = new HashSet<>();
    }

    public synchronized boolean containsLink(String link) {
        boolean result = links.contains(link);
        links.add(link);
        return result;
    }

    public int getSize() {
        return links.size();
    }
}
