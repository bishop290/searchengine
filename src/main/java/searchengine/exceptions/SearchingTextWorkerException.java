package searchengine.exceptions;

public class SearchingTextWorkerException extends RuntimeException {
    public SearchingTextWorkerException() { super("В начале поиска не удалось инициализировать TextWorker."); }
}
