package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.components.JsoupWorker;
import searchengine.components.LemmaSearch;
import searchengine.components.SiteToDbWorker;
import searchengine.components.TextWorker;
import searchengine.dto.searching.PageData;
import searchengine.dto.searching.SearchRequest;
import searchengine.dto.searching.SearchResponse;
import searchengine.managers.SearchManager;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.PageRepository;
import searchengine.tasks.SearchingTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final LemmaSearch lemmaSearch;
    private final SiteToDbWorker siteWorker;
    private final JsoupWorker jsoupWorker;
    private final TextWorker textWorker;
    private final List<SearchingTask> tasks = new ArrayList<>();

    @Override
    public SearchResponse search(SearchRequest request) {
        Map<String, Integer> lemmas = getLemmasInText(request.query());
        if (lemmas.isEmpty()) {
            return null; //леммы не найденны
        } else if (lemmas.size() < 2) {
            return null; //найденно мало лемм (1)
        }
        List<SiteEntity> sites = new ArrayList<>();
        if (request.site() == null) {
            sites.addAll(siteWorker.sites());
        } else {
            sites.add(siteWorker.sites(request.site()));
        }
        if (sites.isEmpty()) {
            return null; //exception
        }
        for (SiteEntity site : sites) {
            SearchManager manager = new SearchManager(
                    site, lemmaSearch, pageRepository, indexRepository, jsoupWorker, textWorker);
            SearchingTask task = new SearchingTask(manager, lemmas);
            task.start();
            tasks.add(task);
        }
        tasks.forEach(SearchingTask::join);
        return prepareResponse();
    }

    private Map<String, Integer> getLemmasInText(String text) {
        try {
            textWorker.init();
        } catch (IOException e) {
            e.getMessage(); /* возврат исключения */
        }
        return textWorker.lemmas(text);
    }

    private SearchResponse prepareResponse() {
        int count = 0;
        List<PageData> allData = new ArrayList<>();
        for (SearchingTask task : tasks) {
            List<PageData> currentData = task.data();
            count += currentData.size();
            allData.addAll(currentData);
        }
        return new SearchResponse(true, count, allData);
    }
}
