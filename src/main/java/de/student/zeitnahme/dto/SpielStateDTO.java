package de.student.zeitnahme.dto;

import de.student.zeitnahme.entity.Spielphase;

import java.util.List;

/**
 * Das ist die einzige Form, in der ein Spielstand nach aussen (REST und
 * spaeter WebSocket) verschickt wird - nie die JPA-Entities direkt, wegen
 * Lazy-Loading und zirkulaeren Referenzen (Spiel -> events -> spiel -> ...).
 */
public record SpielStateDTO(
        Long spielId,
        TeamInfo heimTeam,
        TeamInfo gastTeam,
        int scoreHeim,
        int scoreGast,
        int periode,
        int anzahlHalbzeiten,
        Spielphase phase,
        boolean laeuft,
        int restzeitSekunden,
        List<PenaltyInfo> laufendeStrafenHeim,
        List<PenaltyInfo> laufendeStrafenGast,
        int timeoutsProPeriode,
        int timeoutDauerSekunden,
        int verbleibendeTimeoutsHeim,
        int verbleibendeTimeoutsGast,
        Long timeoutTeamId,
        int timeoutRestSekunden
) {
    public record TeamInfo(Long id, String name, String logoPfad) {
    }

    public record PenaltyInfo(
            Long id,
            String spielerName,
            Integer spielerNummer,
            String strafenArt,
            int restSekunden,
            int dauerSekunden
    ) {
    }
}
