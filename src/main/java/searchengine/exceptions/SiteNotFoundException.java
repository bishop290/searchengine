package searchengine.exceptions;

public class SiteNotFoundException extends RuntimeException {
    public final static String message =  "Данных сайтов нет в базе данных.";
    public SiteNotFoundException() { super(message); }
}