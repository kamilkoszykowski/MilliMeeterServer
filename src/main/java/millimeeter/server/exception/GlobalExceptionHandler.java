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

  @ExceptionHandler(InvalidContentTypeException.class)
  public ResponseEntity<Object> handleInvalidContentTypeException(InvalidContentTypeException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    return new ResponseEntity<>(ex.getRootCause().getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
    return new ResponseEntity<>(
        ex.getMessage().replaceFirst(".* \"", "").replace("\"", ""), ex.getStatus());
  }
}
