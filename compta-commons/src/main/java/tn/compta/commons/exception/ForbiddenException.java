package tn.compta.commons.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ComptaException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public ForbiddenException() {
        super("You don't have permission to perform this action", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
