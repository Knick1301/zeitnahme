package de.student.zeitnahme.dto;

public record PenaltyRequest(
        Long teamId,
        Long playerId,
        String spielerNameFreitext,
        Integer spielerNummerFreitext,
        Long penaltyTypeId
) {
}
