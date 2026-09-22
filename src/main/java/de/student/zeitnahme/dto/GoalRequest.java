package de.student.zeitnahme.dto;

public record GoalRequest(
        Long teamId,
        Long playerId,
        String spielerNameFreitext,
        Integer spielerNummerFreitext,
        Long assistPlayerId,
        String assistNameFreitext,
        Integer assistNummerFreitext
) {
}