package searchengine.managers;

import org.jsoup.nodes.Document;

public record JsoupData(String url, int code, Document document, String errorMessage) {
    public boolean isValid() {
        return document != null && code != -1;
    }
}
