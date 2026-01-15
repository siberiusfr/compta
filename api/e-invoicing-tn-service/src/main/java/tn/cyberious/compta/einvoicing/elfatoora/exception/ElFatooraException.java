package tn.cyberious.compta.einvoicing.elfatoora.exception;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

/** Exception thrown during El Fatoora invoice generation, validation, or signature. */
@Getter
public class ElFatooraException extends RuntimeException {

  private final ErrorCode errorCode;
  private final List<String> details;

  public ElFatooraException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = Collections.emptyList();
  }

  public ElFatooraException(ErrorCode errorCode, String detail) {
    super(errorCode.getMessage() + ": " + detail);
    this.errorCode = errorCode;
    this.details = List.of(detail);
  }

  public ElFatooraException(ErrorCode errorCode, List<String> details) {
    super(errorCode.getMessage() + ": " + String.join(", ", details));
    this.errorCode = errorCode;
    this.details = details;
  }

  public ElFatooraException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.errorCode = errorCode;
    this.details =
        cause.getMessage() != null ? List.of(cause.getMessage()) : Collections.emptyList();
  }

  public ElFatooraException(ErrorCode errorCode, String detail, Throwable cause) {
    super(errorCode.getMessage() + ": " + detail, cause);
    this.errorCode = errorCode;
    this.details = List.of(detail);
  }

  public ElFatooraException(ErrorCode errorCode, String message, List<String> details) {
    super(message + ": " + String.join("; ", details));
    this.errorCode = errorCode;
    this.details = details;
  }
}
