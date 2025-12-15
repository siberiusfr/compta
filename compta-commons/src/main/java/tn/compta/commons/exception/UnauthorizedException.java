package tn.compta.commons.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ComptaException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("Authentication is required to access this resource", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
