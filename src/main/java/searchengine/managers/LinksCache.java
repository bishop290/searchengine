package searchengine.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LinksCache {
    private final long limit;
    private final long weightBorder;
    private final Map<String, Long> links;
    private long counter;

    public LinksCache(long limit, long weightBorder) {
        this.limit = limit;
        this.weightBorder = weightBorder;
        this.counter = 0;
        this.links = new ConcurrentHashMap<>();
    }

    public boolean containsLink(String link) {
        boolean result = links.containsKey(link);
        addWeight(result, link);
        recount();
        return result;
    }

    private void addWeight(boolean contains, String link) {
        if (contains) {
            long weight = links.get(link);
            long newWeight = Long.MAX_VALUE == weight ? weight : weight + 1;
            links.put(link, newWeight);
        } else {
            links.put(link, 0L);
        }
        counter++;
    }

    private void recount() {
        if (counter >= limit) {
            for (Map.Entry<String, Long> item : links.entrySet()) {
                if (item.getValue() < weightBorder) {
                    links.remove(item.getKey());
                }
            }
            counter = 0;
        }
    }
}
