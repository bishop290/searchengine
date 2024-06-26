package searchengine.units;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import searchengine.managers.PageJsoupManager;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("\"PageJsoupManager\" unit tests")
class PageJsoupManagerTest {
    private String domain;
    private Connection connection;
    private PageJsoupManager jsoup;

    @BeforeEach
    void createPageParsingTask() {
        connection = Mockito.mock(Connection.class);
    }

    public static Stream<Arguments> urlProvider() {
        return Stream.of(
                arguments("https://google.com", "/"),
                arguments("https://google.com/", "/"),
                arguments("https://google.com/minor/hello-kitty", "/minor/hello-kitty")
        );
    }

    @ParameterizedTest
    @DisplayName("Cut url address when domain url \"https://google.com\"")
    @MethodSource("urlProvider")
    void testCutUrl(String url, String expected) {
        domain = "https://google.com";
        jsoup = new PageJsoupManager(connection, 1);
        assertEquals(expected, jsoup.getPath(url, domain));
    }

    @ParameterizedTest
    @DisplayName("Cut url address when domain url \"https://google.com/\"")
    @MethodSource("urlProvider")
    void testCutUrlSecondDomain(String url, String expected) {
        domain = "https://google.com/";
        jsoup = new PageJsoupManager(connection, 1);
        assertEquals(expected, jsoup.getPath(url, domain));
    }

    @Test
    @DisplayName("Get links from test page")
    void testGetLinks() throws IOException {
        int numberOfLinks = 10;
        domain = "https://sendel.ru";
        String testPage = "src/test/resources/testpage.html";
        jsoup = new PageJsoupManager(connection, 1);
        File input = new File(testPage);
        Document doc = Jsoup.parse(input, "UTF-8", domain);
        jsoup.setDocument(doc);
        assertEquals(numberOfLinks, jsoup.getLinks(domain).size());
    }
}