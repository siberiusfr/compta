package tn.compta.commons.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ComptaException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ComptaException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public ComptaException(String message, HttpStatus status) {
        this(message, status, status.name());
    }
}
