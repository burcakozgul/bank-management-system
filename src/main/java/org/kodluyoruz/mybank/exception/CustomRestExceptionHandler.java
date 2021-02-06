package org.kodluyoruz.mybank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({CustomerException.class})
    public ResponseEntity<Object> handleApiException(final CustomerException ex) {
        final ApiError apiError;

        apiError = createApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getHttpStatus());
    }

    @ExceptionHandler({AccountException.class})
    public ResponseEntity<Object> handleApiException(final AccountException ex) {
        final ApiError apiError;

        apiError = createApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getHttpStatus());
    }

    @ExceptionHandler({BankCardException.class})
    public ResponseEntity<Object> handleApiException(final BankCardException ex) {
        final ApiError apiError;

        apiError = createApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getHttpStatus());
    }
    @ExceptionHandler({CreditCardException.class})
    public ResponseEntity<Object> handleApiException(final CreditCardException ex) {
        final ApiError apiError;

        apiError = createApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getHttpStatus());
    }

    private ApiError createApiError(HttpStatus httpStatus, String error) {
        ApiError apiError = new ApiError();
        apiError.setHttpStatus(httpStatus);
        apiError.setError(httpStatus.getReasonPhrase());
        apiError.setStatus(httpStatus.value());
        apiError.setMessage(error);
        return apiError;
    }
}


