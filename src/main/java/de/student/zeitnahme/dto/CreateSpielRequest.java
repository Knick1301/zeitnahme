package de.student.zeitnahme.dto;

public record CreateSpielRequest(
        Long heimTeamId,
        Long gastTeamId,
        Integer anzahlHalbzeiten,
        Integer halbzeitDauerSekunden,
        Integer timeoutsProPeriode,
        Integer timeoutDauerSekunden
) {
}
