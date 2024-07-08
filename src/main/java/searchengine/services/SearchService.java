package searchengine.services;

import searchengine.dto.searching.SearchRequest;
import searchengine.dto.searching.SearchResponse;

public interface SearchService {
    SearchResponse search(SearchRequest request);
}
