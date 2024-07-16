package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.comparators.PageDataComparator;
import searchengine.components.Database;
import searchengine.components.JsoupWorker;
import searchengine.components.LemmaSearch;
import searchengine.components.TextWorker;
import searchengine.dto.searching.PageData;
import searchengine.dto.searching.SearchRequest;
import searchengine.dto.searching.SearchResponse;
import searchengine.exceptions.ParsingQueryException;
import searchengine.exceptions.SearchingTextWorkerException;
import searchengine.exceptions.SiteNotFoundException;
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
    private final Database database;
    private final LemmaSearch lemmaSearch;
    private final JsoupWorker jsoupWorker;
    private final TextWorker textWorker;

    @Override
    public SearchResponse search(SearchRequest request) {
        Map<String, Integer> lemmas = getLemmasInText(request.query());
        if (lemmas.isEmpty()) {
            throw new ParsingQueryException("Не удалось получить леммы из текста запроса.");
        } else if (lemmas.size() < 2) {
            throw new ParsingQueryException("Количество распознанных лемм меньше двух");
        }

        List<SiteEntity> sites = new ArrayList<>();
        if (request.site() == null) {
            sites.addAll(database.sites());
        } else {
            sites.add(database.sites(request.site()));
        }
        if (sites.isEmpty()) {
            throw new SiteNotFoundException();
        }

        List<SearchingTask> tasks = new ArrayList<>();
        search(sites, lemmas, tasks);
        return prepareResponse(tasks);
    }

    private Map<String, Integer> getLemmasInText(String text) {
        try {
            textWorker.init();
        } catch (IOException e) {
            throw new SearchingTextWorkerException();
        }
        return textWorker.lemmas(text);
    }

    private void search(List<SiteEntity> sites, Map<String, Integer> lemmas, List<SearchingTask> tasks) {
        for (SiteEntity site : sites) {
            SearchManager manager = new SearchManager(
                    site, lemmaSearch, pageRepository, indexRepository, jsoupWorker, textWorker);
            SearchingTask task = new SearchingTask(manager, lemmas);
            task.start();
            tasks.add(task);
        }
        tasks.forEach(SearchingTask::join);
    }

    private SearchResponse prepareResponse(List<SearchingTask> tasks) {
        int count = 0;
        List<PageData> allData = new ArrayList<>();
        for (SearchingTask task : tasks) {
            count += task.data().size();
            allData.addAll(task.data());
        }
        if (allData.isEmpty()) {
            return new SearchResponse(true, 0, new ArrayList<>());
        }
        return new SearchResponse(true, count, calculateRelativeRelevance(allData));
    }

    public List<PageData> calculateRelativeRelevance(List<PageData> data) {
        data.sort(new PageDataComparator());
        float maxRelevance = data.get(0).getRelevance();
        for (PageData page : data) {
            page.setRelevance(page.getRelevance() / maxRelevance);
        }
        return data;
    }
}