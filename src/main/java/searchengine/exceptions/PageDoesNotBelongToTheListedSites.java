package searchengine.exceptions;

public class PageDoesNotBelongToTheListedSites extends RuntimeException {
    public final static String MESSAGE =
            "Данная страница находится за пределами сайтов, " +
            "указанных в конфигурационном файле";
    public PageDoesNotBelongToTheListedSites() { super(MESSAGE); }
}


