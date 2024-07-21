package searchengine.exceptions;

public class SiteIsNotIndexedException extends RuntimeException {
    public static final String MESSAGE = "В запросе есть сайт/сайты с не законченной индексацией";

    public SiteIsNotIndexedException() {
        super(MESSAGE);
    }
}
