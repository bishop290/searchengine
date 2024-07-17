package searchengine.dto.statistics;

import lombok.Data;

@Data
public class TotalStatistics {
    private int sites;
    private int pages;
    private int lemmas;
    private boolean indexing;

    {
        sites = 0;
        pages = 0;
        lemmas = 0;
        indexing = false;
    }
}
