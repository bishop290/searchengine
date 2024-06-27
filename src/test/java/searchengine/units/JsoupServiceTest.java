package searchengine.units;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import searchengine.config.JsoupSettings;
import searchengine.services.JsoupService;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@RequiredArgsConstructor
@DisplayName("\"JsoupService\" unit tests")
class JsoupServiceTest {
    private JsoupService jsoupService;
    private String domain;

    @BeforeEach
    public void init() {
        jsoupService = new JsoupService(new JsoupSettings());
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
        assertEquals(expected, jsoupService.getPath(url, domain));
    }

    @ParameterizedTest
    @DisplayName("Cut url address when domain url \"https://google.com/\"")
    @MethodSource("urlProvider")
    void testCutUrlSecondDomain(String url, String expected) {
        domain = "https://google.com/";
        assertEquals(expected, jsoupService.getPath(url, domain));
    }

    @Test
    @DisplayName("Get links from test page")
    void testGetLinks() throws IOException {
        int numberOfLinks = 10;
        domain = "https://sendel.ru";
        String testPage = "src/test/resources/testpage.html";
        File input = new File(testPage);
        Document doc = Jsoup.parse(input, "UTF-8", domain);
        jsoupService.setDocument(doc);
        assertEquals(numberOfLinks, jsoupService.getLinks(domain).size());
    }
}