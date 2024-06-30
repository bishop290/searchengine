package searchengine.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.managers.TextManager;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("\"TextManager\" unit tests")
class TextManagerTest {
    String text = """
            Повторное появление леопарда в Осетии позволяет предположить,
            что леопард постоянно обитает в некоторых районах Северного
            Кавказа.
            """;
    String html = """
            <footer class="site-footer">
                <section class="copyright">
                    &copy;
                    2024 Konstantin Shibkov
                </section>
                <section class="powerby">
                    Создано при помощи <a href="https://gohugo.io/" target="_blank" rel="noopener">Hugo</a> <br />
                    Тема <b><a href="https://github.com/CaiJimmy/hugo-theme-stack" target="_blank" rel="noopener" data-version="3.21.0">Stack</a></b>, дизайн <a href="https://jimmycai.com" target="_blank" rel="noopener">Jimmy</a>
                </section>
                <слон>
            </footer>
            """;
    private TextManager manager;

    @BeforeEach
    void init()  {
        manager = new TextManager();
    }

    @Test
    @DisplayName("Text to lemmas")
    void testLemmas() {
        int size = 12;
        int numberOfLeopards = 2;
        String word = "леопард";

        manager.init();
        Map<String, Integer> lemmas = manager.lemmas(text);

        assertEquals(size, lemmas.size());
        assertEquals(numberOfLeopards, lemmas.get(word));
    }
}