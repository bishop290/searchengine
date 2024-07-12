package searchengine.units;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.components.JsoupWorker;
import searchengine.config.JsoupSettings;
import searchengine.managers.PageText;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@RequiredArgsConstructor
@DisplayName("\"JsoupWorker\" unit tests")
class JsoupWorkerTest {
    private JsoupWorker jsoupWorker;
    private String domain;

    @BeforeEach
    public void init() {
        jsoupWorker = new JsoupWorker(new JsoupSettings());
    }

    @Test
    @DisplayName("Get links from test page")
    void testGetLinks() throws IOException {
        int numberOfLinks = 10;
        domain = "https://sendel.ru";
        String testPage = "src/test/resources/testpage.html";
        File input = new File(testPage);
        Document doc = Jsoup.parse(input, "UTF-8", domain);
        List<String> links = jsoupWorker.getLinks(doc, domain);
        assertEquals(numberOfLinks, links.size());
    }

    @Test
    @DisplayName("Get text from html")
    void testGetTextFromHtml() throws IOException {
        String title = "Konstantin Shibkov";
        String testPage = "src/test/resources/testpage.html";
        String html = Files.readString(Path.of(testPage), Charset.defaultCharset());

        PageText data = jsoupWorker.getTextFromHtml(html);
        assertEquals(title, data.title());
        assertNotEquals("", data.body());
    }
}