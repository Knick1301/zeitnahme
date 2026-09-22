package de.student.zeitnahme.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Historischer Log-Eintrag - bleibt unveraendert stehen, sobald er einmal
 * angelegt ist (z.B. fuer einen spaeteren Spielbericht). Laufende
 * Zeitstrafen werden NICHT hier veraendert, sondern in ActivePenalty
 * (siehe dort).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class GameEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @ManyToOne
    @JoinColumn(name = "spiel_id", nullable = false)
    private Spiel spiel;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // Torschuetze bzw. bestrafter Spieler. Freitext-Felder als Fallback,
    // falls der Gegner-Spieler nicht als Player-Datensatz existiert.
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
    private String spielerNameFreitext;
    private Integer spielerNummerFreitext;

    // nur bei GOAL relevant
    @ManyToOne
    @JoinColumn(name = "assist_player_id")
    private Player assistPlayer;

    // nur bei PENALTY relevant
    @ManyToOne
    @JoinColumn(name = "penalty_type_id")
    private PenaltyType penaltyType;

    // Spielzeit, zu der das Ereignis passiert ist (Sekunden ab Spielbeginn)
    private int spielzeitSekunden;
}
