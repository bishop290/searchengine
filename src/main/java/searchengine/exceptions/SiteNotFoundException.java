package searchengine.exceptions;

public class SiteNotFoundException extends RuntimeException {
    public static final String MESSAGE =  "Данных сайтов нет в базе данных.";
    public SiteNotFoundException() { super(MESSAGE); }
}