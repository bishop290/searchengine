package searchengine.dto.searching;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class PageData {
    String site;
    String siteName;
    String uri;
    String title;
    String snippet;
    float relevance;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageData pageData = (PageData) o;
        return Objects.equals(site, pageData.site) && Objects.equals(uri, pageData.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, uri);
    }
}
