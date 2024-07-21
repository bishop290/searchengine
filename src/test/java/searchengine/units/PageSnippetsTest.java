package searchengine.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.components.JsoupWorker;
import searchengine.config.JsoupSettings;
import searchengine.integration.tools.DbHelper;
import searchengine.managers.PageSnippets;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("\"PageSnippets\" unit tests")
class PageSnippetsTest {
    private PageSnippets pageSnippets;

    @BeforeEach
    public void init() { pageSnippets = new PageSnippets(); }

    @Test
    @DisplayName("Add lemmas")
    void addLemmas() {
        pageSnippets.addLemmas(Arrays.asList("lemma1", "lemma2", "lemma3"));
        assertEquals(3, pageSnippets.getLemmas().size());
    }

    @Test
    @DisplayName("Add snippet")
    void addSnippet() {
        String rawSnippet = "но  никак  некомментирует.     Зачем ему говорить о гоми? Это его  среда обитания, воздух,  которым ондышит.  Всю свою жизнь он плавает в гоми как рыба в воде.  Рубин мотается поокруге в  своем  грузовике-развалюхе,  переделанном из древнего аэродромного\"мерседеса\", крышу которого  закрывает огромны";
        String expect = "...но  никак  некомментирует.     Зачем ему говорить о <b>гоми</b>? Это его  среда обитания, <b>воздух</b>,  которым ондышит.  Всю свою <b>жизнь</b> он плавает в <b>гоми</b> как рыба в воде.  Рубин мотается поокруге в  своем  грузовике-развалюхе,  переделанном из древнего аэродромного\"мерседеса\", крышу которого  закрывает огромны...<br>";
        pageSnippets.addLemmas(Arrays.asList("гоми", "воздух", "жизнь"));
        pageSnippets.addSnippet(rawSnippet);
        String result = pageSnippets.getSnippets().get(0).getSnippet();
        assertEquals(1, pageSnippets.getSnippets().size());
        assertEquals(expect, result);
    }

    @Test
    @DisplayName("Get snippets size")
    void snippetsSize() {
        String rawSnippet = "snippet";
        pageSnippets.addSnippet(rawSnippet);
        assertEquals(1, pageSnippets.snippetsSize());
    }

    @Test
    @DisplayName("Set empty title")
    void setEmptyTitle() {
        String expect = "заголовок отсутствует";
        pageSnippets.setTitle("");
        String result = pageSnippets.getTitle();
        assertEquals(expect, result);
    }

    @Test
    @DisplayName("Set title")
    void setTitle() {
        String title = "лучший заголовок";
        pageSnippets.setTitle(title);
        String result = pageSnippets.getTitle();
        assertEquals(title, result);
    }

    @Test
    @DisplayName("Set absolute relevance")
    void setAbsoluteRelevance() {
        pageSnippets.setAbsoluteRelevance(10);
        pageSnippets.setAbsoluteRelevance(10);
        pageSnippets.setAbsoluteRelevance(10);
        pageSnippets.setAbsoluteRelevance(10);
        assertEquals(40, pageSnippets.getAbsoluteRelevance());
    }

    @Test
    @DisplayName("Set relative relevance")
    void setRelativeRelevance() {
        pageSnippets.addSnippet("snippet1");
        pageSnippets.addSnippet("snippet2");
        pageSnippets.setAbsoluteRelevance(20);
        pageSnippets.setRelativeRelevance(20);
        assertEquals(1, pageSnippets.getSnippets().get(0).getRelevance());
    }
}