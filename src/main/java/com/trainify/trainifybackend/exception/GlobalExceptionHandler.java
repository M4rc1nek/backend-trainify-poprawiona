package com.trainify.trainifybackend.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice // -> Działa globalnie na wszystkie kontrolery, zwraca JSON
public class GlobalExceptionHandler {


    /*
           CONFLICT (409) → konflikt danych (np. email już istnieje)
           NOT_FOUND (404) → brak danych (np. użytkownik nie istnieje)
           BAD_REQUEST (400) → błędne dane wejściowe (np. niepoprawny format)
           UNAUTHORIZED (401) → nieprawidłowe logowanie lub brak autoryzacji


    webRequest.getDescription(false) - Pobiera opis żądania path, np.  /register lub /login
    Jeśli dasz true, wynik zawierałby też informacje o kliencie (np. IP, host)
     */

    @ExceptionHandler({EmailAlreadyExistsException.class, UsernameAlreadyExistsException.class, DailyWellnessAlreadySubmittedException.class})
    public ResponseEntity<ErrorResponseDTO> handleConflictExceptions(RuntimeException exception, WebRequest webRequest) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                webRequest.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }


    @ExceptionHandler({TrainingForUserNotFoundException.class, UserNotFoundException.class, DailyWellnessForUserNotFoundException.class, UserHealthMetricsNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleNotFoundExceptions(RuntimeException exception, WebRequest webRequest) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                webRequest.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ErrorResponseDTO> handleWrongPassword(WrongPasswordException exception, WebRequest webRequest) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                exception.getMessage(),
                webRequest.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAll(Exception exception, WebRequest webRequest) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Wystąpił nieoczekiwany błąd. Skontaktuj się z administratorem.",
                webRequest.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }



}
