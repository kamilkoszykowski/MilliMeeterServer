package millimeeter.server.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.apache.tomcat.util.http.fileupload.impl.InvalidContentTypeException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    Map<String, List<String>> body = new HashMap<>();
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.toList());
    body.put("errors", errors);
    if (body.toString().contains("required")) {
      return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    } else {
      return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolationException(
      ConstraintViolationException ex) {
    Map<String, List<String>> body = new HashMap<>();
    List<String> errors =
        ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
    body.put("errors", errors);
    if (body.toString().contains("required")) {
      return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    } else {
      return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {
    Map<String, List<String>> body = new HashMap<>();
    String message = ex.getMessage();
    if (message != null) {
      body.put(
          "errors",
          List.of(
              "Bad argument type. "
                  + message.replaceAll("Failed to convert.*?java.lang.", "").replaceAll("'.*", " '")
                  + message.replaceAll(".*For input .*?: \"", "").replaceAll("\".*", "'")
                  + " instead of "
                  + message
                      .replaceAll(".*required type '(java.lang.|)", "")
                      .replaceAll("'.*", "")));
    }
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidContentTypeException.class)
  public ResponseEntity<Object> handleInvalidContentTypeException(InvalidContentTypeException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
      MissingServletRequestPartException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    Map<String, List<String>> body = new HashMap<>();
    body.put("errors", List.of(ex.getMessage()));
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    return new ResponseEntity<>(ex.getRootCause().getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
    Map<String, List<String>> body = new HashMap<>();
    body.put("errors", List.of(ex.getMessage().replaceFirst(".* \"", "").replace("\"", "")));
    return new ResponseEntity<>(body, ex.getStatus());
  }
}
