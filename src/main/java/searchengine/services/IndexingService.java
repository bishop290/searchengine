package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;

import java.io.UnsupportedEncodingException;

public interface IndexingService {
    IndexingResponse start();
    IndexingResponse stop();
    IndexingResponse startOnePage(String url);
}
