package searchengine.managers;

import lombok.Getter;
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
public class PageJsoupManager {
    private final String url;
    private final Connection connection;
    private final String domain;

    @Setter
    private Document document;
    private int code;
    private String body;
    private boolean error;

    public PageJsoupManager(String url, Connection connection, String domain) {
        this.url = url;
        this.connection = connection;
        this.domain = domain.replaceFirst("^*(/)$", "");
        this.error = false;
    }

    public void connect() {
        try {
            Connection.Response response = connection.newRequest().url(url).execute();
            document = response.parse();
            set(false, document, response.statusCode(), document.body().html());
        } catch (HttpStatusException e) {
            set(true, null, e.getStatusCode(), e.getMessage());
        } catch (IOException e) {
            set(true, null, -1, e.getMessage());
        }
    }

    public List<String> getLinks() {
        if (document == null) {
            return new ArrayList<>();
        }
        Set<String> urls = new HashSet<>();
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String absLink = link.attr("abs:href");
            if (isUrlValid(absLink) && isNotMainUrl(absLink) && isNotDomain(absLink)) {
                urls.add(absLink);
            }
        }
        return urls.stream().toList();
    }

    public String getPath() {
        if (url == null) {
            return "";
        } else if (url.equals(domain) || url.equals(domain + "/")) {
            return "/";
        } else if (!isUrlValid(url)) {
            return "";
        } else {
            return url.replaceFirst(domain, "");
        }
    }

    public PageJsoupManager getChild(String url) {
        return new PageJsoupManager(url, connection, domain);
    }

    private boolean isUrlValid(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.startsWith(domain + "/") && !url.contains("#");
    }

    private boolean isNotMainUrl(String url) {
        return !url.equals(this.url) && !url.equals(this.url + "/");
    }

    private boolean isNotDomain(String url) {
        return !url.equals(domain) && !url.equals(domain + "/");
    }

    private void set(boolean error, Document document, int code, String body) {
        this.error = error;
        this.document = document;
        this.code = code;
        this.body = body;
    }
}
