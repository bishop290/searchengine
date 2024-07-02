package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.SiteStatistics;

public interface SiteStatisticsRepository extends JpaRepository<SiteStatistics, Integer> {
}
