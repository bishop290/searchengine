package searchengine.dto.searching;

public record SearchRequest(String query, String site, int offset, int limit) {
}
