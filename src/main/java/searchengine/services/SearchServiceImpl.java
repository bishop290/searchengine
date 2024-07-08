package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.searching.SearchRequest;
import searchengine.dto.searching.SearchResponse;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    @Override
    public SearchResponse search(SearchRequest request) {
        return null;
    }
}
