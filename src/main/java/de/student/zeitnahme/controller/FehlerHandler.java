package de.student.zeitnahme.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Macht aus fachlichen Fehlern saubere HTTP-Antworten statt 500:
 * ungueltige Eingaben (unbekanntes Spiel/Team) -> 400,
 * nicht erlaubte Aktionen im aktuellen Zustand (z.B. kein Timeout mehr) -> 409.
 */
@RestControllerAdvice
public class FehlerHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> ungueltigeEingabe(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("fehler", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> nichtErlaubt(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("fehler", e.getMessage()));
    }
}
