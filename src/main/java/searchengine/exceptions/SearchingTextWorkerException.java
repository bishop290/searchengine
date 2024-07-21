package searchengine.exceptions;

public class SearchingTextWorkerException extends RuntimeException {
    public static final String MESSAGE =
            "В начале поиска не удалось инициализировать TextWorker.";

    public SearchingTextWorkerException() {
        super(MESSAGE);
    }
}
