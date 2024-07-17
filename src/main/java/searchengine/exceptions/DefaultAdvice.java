package searchengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import searchengine.dto.indexing.IndexingResponseError;
import searchengine.dto.searching.SearchResponseError;

@ControllerAdvice
public class DefaultAdvice {
    @ExceptionHandler(IndexingIsNotRunningException.class)
    public ResponseEntity<IndexingResponseError> handleIndexingIsNotRunningException(IndexingIsNotRunningException e) {
        return new ResponseEntity<>(
                new IndexingResponseError(false, e.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(IndexingIsAlreadyRunningException.class)
    public ResponseEntity<IndexingResponseError> handleIndexingIsAlreadyRunningException(IndexingIsAlreadyRunningException e) {
        return new ResponseEntity<>(
                new IndexingResponseError(false, e.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(IndexingTextWorkerException.class)
    public ResponseEntity<IndexingResponseError> handleIndexingTextWorkerException(IndexingTextWorkerException e) {
        return new ResponseEntity<>(
                new IndexingResponseError(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PageDoesNotBelongToTheListedSites.class)
    public ResponseEntity<IndexingResponseError> handlePageDoesNotBelongToTheListedSites(PageDoesNotBelongToTheListedSites e) {
        return new ResponseEntity<>(
                new IndexingResponseError(false, e.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(ParsingQueryException.class)
    public ResponseEntity<SearchResponseError> handleParsingQueryException(ParsingQueryException e) {
        return new ResponseEntity<>(
                new SearchResponseError(false, e.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(SiteNotFoundException.class)
    public ResponseEntity<SearchResponseError> handleSiteNotFoundException(SiteNotFoundException e) {
        return new ResponseEntity<>(
                new SearchResponseError(false, e.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(SearchingException.class)
    public ResponseEntity<SearchResponseError> handleSearchingException(SearchingException e) {
        return new ResponseEntity<>(
                new SearchResponseError(false, e.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(SearchingTextWorkerException.class)
    public ResponseEntity<SearchResponseError> handleSearchingTextWorkerException(SearchingTextWorkerException e) {
        return new ResponseEntity<>(
                new SearchResponseError(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
