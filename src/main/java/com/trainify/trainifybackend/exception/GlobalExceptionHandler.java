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
           UNPROCESSABLE_ENTITY (422) ->  dane są poprawne, ale operacja nie może zostać wykonana z powodów biznesowych (brakuje czegoś ważnego)
                          (np. próba obliczenia TDEE bez wcześniejszego obliczenia BMR)

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


    @ExceptionHandler(InvalidEnumValueException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidEnumValue(InvalidEnumValueException exception, WebRequest webRequest) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(),
                webRequest.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MissingRequirementException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingRequirement(MissingRequirementException exception, WebRequest webRequest){
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                exception.getMessage(),
                webRequest.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
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
