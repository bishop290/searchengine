package searchengine.dto.searching;

import java.util.List;

public record SearchResponse(boolean result, int count, List<Snippet> data) {
}
