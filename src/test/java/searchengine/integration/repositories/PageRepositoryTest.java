package searchengine.integration.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import searchengine.integration.tools.DatabaseWorker;
import searchengine.integration.tools.IntegrationTest;
import searchengine.integration.tools.TestContainer;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
@RequiredArgsConstructor
@DisplayName("\"PageRepository\" integration tests")
class PageRepositoryTest extends TestContainer {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final EntityManager entityManager;
    private final NamedParameterJdbcTemplate jdbc;

    private String path;
    private Integer code;
    private String content;
    private String siteUrl;
    private SiteEntity site;

    @BeforeEach
    public void setSite() {
        path = "www.google.com/hot-sausage-pie";
        code = 404;
        content = "Hello world!";
        siteUrl = "www.google.com";

        site = SiteEntity.builder()
                .status(Status.INDEXING)
                .statusTime(new Timestamp(System.currentTimeMillis()))
                .lastError("This is last error")
                .url(siteUrl)
                .name("Google").build();

        DatabaseWorker.saveToDb(site, siteRepository, entityManager);
    }

    @Test
    @DisplayName("Save \"Page\" entity to db")
    public void testSaveToDb() {
        PageEntity page = PageEntity.builder()
                .site(site).path(path).code(code).content(content).build();

        DatabaseWorker.saveToDb(site, siteRepository, entityManager);
        DatabaseWorker.saveToDb(page, pageRepository, entityManager);
        PageEntity savedPage = DatabaseWorker.get(PageEntity.class, jdbc);

        assertEquals(savedPage.getPath(), path);
        assertEquals(savedPage.getCode(), code);
        assertEquals(savedPage.getContent(), content);
    }

    @Test
    @DisplayName("Find Path entities by site and paths")
    public void testFindBySiteAndPathIn() {
        String tesPath1 = "www.google.com/hot-sausage-pie2";
        String tesPath2 = "www.google.com/hot-sausage-pie3";
        String tesPath3 = "www.google.com/hot-sausage-pie10";
        long result = 2;
        int numberOfEntities = 6;

        for (int i = 0; i < numberOfEntities; i++) {
            PageEntity page = PageEntity.builder()
                    .site(site).path(path + i).code(code).content(content).build();
            DatabaseWorker.saveToDb(page, pageRepository, entityManager);
        }
        Set<String> paths = new TreeSet<>();
        paths.add(tesPath1);
        paths.add(tesPath2);
        paths.add(tesPath3);

        List<PageEntity> entities = pageRepository.findBySiteAndPathIn(site, paths);

        assertEquals(result, entities.size());
    }
}