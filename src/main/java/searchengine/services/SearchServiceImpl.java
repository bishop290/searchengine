package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.comparators.PageSnippetsComparator;
import searchengine.components.*;
import searchengine.dto.searching.SearchRequest;
import searchengine.dto.searching.SearchResponse;
import searchengine.dto.searching.Snippet;
import searchengine.exceptions.ParsingQueryException;
import searchengine.exceptions.SearchingTextWorkerException;
import searchengine.exceptions.SiteIsNotIndexedException;
import searchengine.exceptions.SiteNotFoundException;
import searchengine.managers.PageSnippets;
import searchengine.managers.SearchManager;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.PageRepository;
import searchengine.tasks.SearchingTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final Database database;
    private final LemmaSearch lemmaSearch;
    private final JsoupWorker jsoupWorker;
    private final TextWorker textWorker;
    private final SearchingCache cache;

    @Override
    public SearchResponse search(SearchRequest request) {
        SearchResponse quickResponse = cache.get(request);
        if (quickResponse != null) {
            return quickResponse;
        }
        List<SiteEntity> sites = getSites(request);
        Map<String, Integer> lemmas = getLemmas(request);
        List<String> words = textWorker.validWords(request.query());

        List<SearchingTask> tasks = new ArrayList<>();
        search(sites, words, lemmas, tasks);
        SearchResponse response = prepareResponse(tasks);

        cache.add(request, response);
        return cache.get(request);
    }

    private List<SiteEntity> getSites(SearchRequest request) {
        List<SiteEntity> sites = new ArrayList<>();
        if (request.site() == null) {
            sites.addAll(database.sites(Arrays.asList("INDEXING", "INDEXED")));
        } else {
            sites.add(database.sites(request.site()));
        }
        if (sites.isEmpty()) {
            throw new SiteNotFoundException();
        }
        boolean isNotIndexed = sites.stream().anyMatch(
                site -> site.getStatus().equals(Status.INDEXING));
        if (isNotIndexed) {
            throw new SiteIsNotIndexedException();
        }
        return sites;
    }

    private Map<String, Integer> getLemmas(SearchRequest request) {
        Map<String, Integer> lemmas = getLemmasInText(request.query());
        if (lemmas.isEmpty()) {
            throw new ParsingQueryException("Не удалось получить леммы из текста запроса.");
        }
        return lemmas;
    }

    private Map<String, Integer> getLemmasInText(String text) {
        try {
            textWorker.init();
        } catch (IOException e) {
            throw new SearchingTextWorkerException();
        }
        return textWorker.lemmas(text);
    }

    private void search(List<SiteEntity> sites,
                        List<String> words,
                        Map<String, Integer> lemmas,
                        List<SearchingTask> tasks) {
        for (SiteEntity site : sites) {
            SearchManager manager = new SearchManager(
                    site, lemmaSearch, pageRepository, indexRepository, jsoupWorker, textWorker);
            SearchingTask task = new SearchingTask(manager, words, lemmas);
            task.start();
            tasks.add(task);
        }
        tasks.forEach(SearchingTask::join);
    }

    private SearchResponse prepareResponse(List<SearchingTask> tasks) {
        int count = 0;
        List<PageSnippets> allData = new ArrayList<>();
        for (SearchingTask task : tasks) {
            if (!task.data().isEmpty()) {
                count += task.count();
                allData.addAll(task.data());
            }
        }
        return new SearchResponse(true, count, calculateRelativeRelevance(allData));
    }

    private List<Snippet> calculateRelativeRelevance(List<PageSnippets> data) {
        if (data.isEmpty()) {
            return new ArrayList<>();
        }
        List<Snippet> snippets = new ArrayList<>();
        data.sort(new PageSnippetsComparator());

        float maxRelevance = data.get(0).getAbsoluteRelevance();
        for (PageSnippets pageSnippets : data) {
            pageSnippets.setRelativeRelevance(maxRelevance);
            snippets.addAll(pageSnippets.getSnippets());
        }
        return snippets;
    }
}