package searchengine.exceptions;

public class IndexingTextWorkerException extends RuntimeException {
    public static final String MESSAGE =
            "При старте индексации не удалось инициализировать TextWorker.";

    public IndexingTextWorkerException() {
        super(MESSAGE);
    }
}
