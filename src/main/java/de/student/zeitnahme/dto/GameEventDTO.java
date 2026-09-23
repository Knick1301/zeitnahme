package de.student.zeitnahme.dto;

public record GameEventDTO(
        String type,
        Long teamId,
        String teamName,
        String spielerName,
        Integer spielerNummer,
        String assistName,
        Integer assistNummer,
        String strafenArt,
        Integer strafenDauerSekunden,
        int periode,
        int spielzeitSekunden
) {
}