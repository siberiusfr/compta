package tn.cyberious.compta.einvoicing.elfatoora.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler for El Fatoora module. */
@Slf4j
@RestControllerAdvice(basePackages = "tn.cyberious.compta.einvoicing.elfatoora")
public class ElFatooraExceptionHandler {

  @ExceptionHandler(ElFatooraException.class)
  public ResponseEntity<ErrorResponse> handleElFatooraException(ElFatooraException ex) {
    log.error("El Fatoora exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode().getCode())
            .message(ex.getErrorCode().getMessage())
            .details(ex.getDetails())
            .timestamp(LocalDateTime.now())
            .build();

    HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());
    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    log.warn("Validation exception: {}", ex.getMessage());

    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.toList());

    ErrorResponse response =
        ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Request validation failed")
            .details(details)
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code("INVALID_ARGUMENT")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Unexpected error in El Fatoora module", ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .details(List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  private HttpStatus mapErrorCodeToStatus(ErrorCode errorCode) {
    return switch (errorCode) {
      case INVALID_XML_STRUCTURE,
          XSD_VALIDATION_FAILED,
          INVALID_TAX_IDENTIFIER,
          INVALID_DATE_FORMAT,
          MISSING_REQUIRED_FIELD,
          INVALID_DOCUMENT_TYPE,
          INVALID_AMOUNT_FORMAT,
          INVALID_POSTAL_CODE,
          INVALID_TAX_RATE,
          INVALID_CURRENCY,
          XML_PARSING_ERROR,
          UNMARSHALLING_ERROR ->
          HttpStatus.BAD_REQUEST;
      case CERTIFICATE_ERROR, SIGNATURE_FAILED, SIGNATURE_VERIFICATION_FAILED ->
          HttpStatus.INTERNAL_SERVER_ERROR;
      case TAX_CALCULATION_ERROR, MARSHALLING_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

  private String formatFieldError(FieldError error) {
    return error.getField() + ": " + error.getDefaultMessage();
  }

  /** Error response structure. */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class ErrorResponse {
    private String code;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;
  }
}
