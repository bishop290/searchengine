package searchengine.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import searchengine.services.TextService;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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
    private TextService manager;

    @BeforeEach
    void init()  {
        manager = new TextService();
    }

    public static Stream<Arguments> urlProvider() {
        return Stream.of(
                arguments("https://google.com", "/"),
                arguments("https://google.com/", "/"),
                arguments("https://google.com/minor/hello-kitty", "/minor/hello-kitty")
        );
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

    @ParameterizedTest
    @DisplayName("Cut url address when domain url \"https://google.com\"")
    @MethodSource("urlProvider")
    void testCutUrl(String url, String expected) {
        String domain = "https://google.com";
        assertEquals(expected, manager.path(url, domain));
    }

    @ParameterizedTest
    @DisplayName("Cut url address when domain url \"https://google.com/\"")
    @MethodSource("urlProvider")
    void testCutUrlSecondDomain(String url, String expected) {
        String domain = "https://google.com/";
        assertEquals(expected, manager.path(url, domain));
    }
}