package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteStatistics;
import searchengine.repositories.SiteStatisticsRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteStatisticsRepository siteStatisticsRepository;

    @Override
    public StatisticsResponse getStatistics() {
        List<SiteStatistics> statistics = siteStatisticsRepository.findAll();
        if (statistics == null) {
            // исключение может быть
            return new StatisticsResponse();
        }
        StatisticsResponse response = new StatisticsResponse();
        response.setResult(true);
        response.setStatistics(generateData(statistics));
        return response;
    }

    private StatisticsData generateData(List<SiteStatistics> statistics) {
        TotalStatistics totalItem = new TotalStatistics();
        List<DetailedStatisticsItem> detailedItems = new ArrayList<>();

        for (SiteStatistics site : statistics) {
            detailedItems.add(DetailedStatisticsItem.builder()
                    .url(site.getUrl())
                    .name(site.getName())
                    .status(site.getStatus())
                    .statusTime(site.getStatusTime().getTime())
                    .error(site.getError())
                    .pages(site.getPages())
                    .lemmas(site.getLemmas())
                    .build());
            totalItem.setSites(totalItem.getSites() + 1);
            totalItem.setPages(totalItem.getPages() + site.getPages());
            totalItem.setLemmas(totalItem.getLemmas() + site.getLemmas());
        }
        totalItem.setIndexing(true);
        return StatisticsData.builder()
                .total(totalItem)
                .detailed(detailedItems)
                .build();
    }
}
