package searchengine.services;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupSettings;
import searchengine.managers.JsoupData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Service
public class JsoupService {
    private final static int MINIMAL_DELAY = 1000;
    private final JsoupSettings jsoupSettings;
    private final Connection connection;
    private final int delay;

    public JsoupService(JsoupSettings jsoupSettings) {
        this.jsoupSettings = jsoupSettings;
        this.connection = Jsoup.newSession()
                .userAgent(jsoupSettings.getAgent())
                .referrer(jsoupSettings.getReferrer());
        this.delay = Math.max(jsoupSettings.getDelay(), MINIMAL_DELAY);
    }

    public JsoupData connect(String url) {
        startDelay();
        try {
            Connection.Response response = connection.newRequest().url(url).execute();
            Document document = response.parse();
            return new JsoupData(url, response.statusCode(), document, "");
        } catch (HttpStatusException e) {
            return new JsoupData(url, e.getStatusCode(), null, e.getMessage());
        } catch (IOException e) {
            return new JsoupData(url, -1, null, e.getMessage());
        }
    }

    public List<String> getLinks(Document document, String domain) {
        domain = domain.replaceFirst("^*(/)$", "");
        if (document == null) {
            return new ArrayList<>();
        }
        Set<String> urls = new HashSet<>();
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String absLink = link.attr("abs:href");
            if (isUrlValid(absLink, domain) && !isDomain(absLink, domain)) {
                urls.add(absLink);
            }
        }
        return new ArrayList<>(urls);
    }

    public boolean isDomain(String url, String domain) {
        return url.equals(domain) || url.equals(domain + "/");
    }

    private boolean isUrlValid(String url, String domain) {
        String tailRegex = "^.*\\.(jpg|JPG|gif|GIF|doc|DOC|pdf|PDF|png|PNG|jpeg|JPEG)$";
        if (url == null || url.isEmpty()) {
            return false;
        }
        boolean isValidHead = url.startsWith(domain + "/") || url.startsWith("/");
        boolean isValidTail = !url.matches(tailRegex);
        return  isValidHead && isValidTail && !url.contains("#");
    }

    private void startDelay() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
