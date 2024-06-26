package searchengine.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class PageJsoupManager {
    private final Connection connection;
    private final int delay;

    @Setter
    private Document document;
    private int code = -1;
    private String body = "";

    public void connect(String url) {
        startDelay();
        try {
            Connection.Response response = connection.newRequest().url(url).execute();
            document = response.parse();
            set(document, response.statusCode(), document.body().html());
        } catch (HttpStatusException e) {
            set( null, e.getStatusCode(), e.getMessage());
        } catch (IOException e) {
            set(null, -1, e.getMessage());
        }
    }

    public List<String> getLinks(String domain) {
        domain = domain.replaceFirst("^*(/)$", "");
        if (document == null) {
            return new ArrayList<>();
        }
        Set<String> urls = new HashSet<>();
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String absLink = link.attr("abs:href");
            if (isUrlValid(absLink, domain) && isNotDomain(absLink, domain)) {
                urls.add(absLink);
            }
        }
        return urls.stream().toList();
    }

    public String getPath(String url, String domain) {
        domain = domain.replaceFirst("^*(/)$", "");
        if (url.equals(domain) || url.equals(domain + "/")) {
            return "/";
        } else {
            return url.replaceFirst(domain, "");
        }
    }

    private boolean isUrlValid(String url, String domain) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        boolean isValidHead = url.startsWith(domain + "/") || url.startsWith("/");
        boolean isValidTail = !url.endsWith(".jpeg") || !url.endsWith(".jpg") ||
                !url.endsWith(".png") || !url.endsWith(".pdf");
        return  isValidHead && isValidTail && !url.contains("#");
    }

    private boolean isNotDomain(String url, String domain) {
        return !url.equals(domain) && !url.equals(domain + "/");
    }

    private void startDelay() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void set(Document document, int code, String body) {
        this.document = document;
        this.code = code;
        this.body = body;
    }
}
