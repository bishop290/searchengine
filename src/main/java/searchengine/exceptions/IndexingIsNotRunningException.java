package searchengine.exceptions;

public class IndexingIsNotRunningException extends RuntimeException {
    public IndexingIsNotRunningException() { super("Индексация не запущена"); }
}
