package searchengine.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import searchengine.components.TextWorker;
import searchengine.config.SearchSettings;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("\"TextWorker\" unit tests")
class TextWorkerTest {
    String text = """
            Повторное появление леопарда в Осетии позволяет предположить,
            что леопард постоянно обитает в некоторых районах Северного
            Кавказа.
            """;

    private TextWorker worker;

    @BeforeEach
    void init()  {
        worker = new TextWorker(new SearchSettings());
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
    void testLemmas() throws IOException {
        int size = 12;
        int numberOfLeopards = 2;
        String word = "леопард";

        worker.init();
        Map<String, Integer> lemmas = worker.lemmas(text);

        assertEquals(size, lemmas.size());
        assertEquals(numberOfLeopards, lemmas.get(word));
    }

    @ParameterizedTest
    @DisplayName("Cut url address when domain url \"https://google.com\"")
    @MethodSource("urlProvider")
    void testCutUrl(String url, String expected) {
        String domain = "https://google.com";
        assertEquals(expected, worker.path(url, domain));
    }

    @ParameterizedTest
    @DisplayName("Cut url address when domain url \"https://google.com/\"")
    @MethodSource("urlProvider")
    void testCutUrlSecondDomain(String url, String expected) {
        String domain = "https://google.com/";
        assertEquals(expected, worker.path(url, domain));
    }

    @Test
    @DisplayName("URL decode")
    void testUrlDecode() {
        String expected = "https://sendel.ru/books/";
        String url = "url=https%3A%2F%2Fsendel.ru%2Fbooks%2F";
        String result = worker.urlDecode(url);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("URL decode without head")
    void testUrlDecodeWithoutHead() {
        String expected = "https://sendel.ru/books/";
        String url = "url=sendel.ru%2Fbooks%2F";
        String result = worker.urlDecode(url);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("First char from word to upper case")
    void testFirstToUpperCase() {
        String word = "экзоскелет";
        String expect = "Экзоскелет";
        String result = worker.firstCharToUpperCase(word);
        assertEquals(expect, result);
    }
}