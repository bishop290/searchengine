package searchengine.exceptions;

public class IndexingIsAlreadyRunningException extends RuntimeException {
    public IndexingIsAlreadyRunningException() {super("Индексация уже запущена");}
}
