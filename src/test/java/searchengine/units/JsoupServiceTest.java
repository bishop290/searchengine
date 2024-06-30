package searchengine.units;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.config.JsoupSettings;
import searchengine.services.JsoupService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
@DisplayName("\"JsoupService\" unit tests")
class JsoupServiceTest {
    private JsoupService jsoupService;
    private String domain;

    @BeforeEach
    public void init() {
        jsoupService = new JsoupService(new JsoupSettings());
    }

    @Test
    @DisplayName("Get links from test page")
    void testGetLinks() throws IOException {
        int numberOfLinks = 10;
        domain = "https://sendel.ru";
        String testPage = "src/test/resources/testpage.html";
        File input = new File(testPage);
        Document doc = Jsoup.parse(input, "UTF-8", domain);
        List<String> links = jsoupService.getLinks(doc, domain);
        assertEquals(numberOfLinks, links.size());
    }
}