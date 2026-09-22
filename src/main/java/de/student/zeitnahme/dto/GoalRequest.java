package de.student.zeitnahme.dto;

/**
 * playerId gesetzt -> bekannter Spieler (z.B. eigenes Team).
 * Sonst spielerNameFreitext/-NummerFreitext -> Gegner ohne Kader-Eintrag.
 * assistPlayerId ist immer optional.
 */
public record GoalRequest(
        Long teamId,
        Long playerId,
        String spielerNameFreitext,
        Integer spielerNummerFreitext,
        Long assistPlayerId
) {
}
