package com.tickgenerator.handler.exception;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(PartnerServiceException.class)
    public ResponseEntity<String> handlePartnerServiceException(PartnerServiceException ex) {
        log.error("Error Streaming Data from Partner Service", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(JSONException.class)
    public ResponseEntity<String> handleJSONException(JSONException ex) {
        log.error("Error parsing JSON message", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);

    }
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<String> handleAllExceptions(Exception ex) {
        log.error("Internal Server Error", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
