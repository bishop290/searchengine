package searchengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import searchengine.dto.indexing.IndexingResponseError;

@ControllerAdvice
public class DefaultAdvice {
    @ExceptionHandler(IndexingIsAlreadyRunningException.class)
    public ResponseEntity<IndexingResponseError> handleIndexingIsAlreadyRunningException(IndexingIsAlreadyRunningException e) {
        return new ResponseEntity<>(new IndexingResponseError(false, e.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(IndexingIsNotRunningException.class)
    public ResponseEntity<IndexingResponseError> handleIndexingIsNotRunningException(IndexingIsNotRunningException e) {
        return new ResponseEntity<>(new IndexingResponseError(false, e.getMessage()), HttpStatus.OK);
    }
}
