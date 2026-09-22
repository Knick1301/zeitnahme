package de.student.zeitnahme.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lebt nur, solange die Strafzeit laeuft. Wird vom Server-Takt jede
 * Sekunde runtergezaehlt und bei Ablauf geloescht - im Gegensatz zum
 * zugehoerigen GameEvent, das dauerhaft als Protokoll stehen bleibt.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ActivePenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "spiel_id", nullable = false)
    private Spiel spiel;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
    private String spielerNameFreitext;
    private Integer spielerNummerFreitext;

    private String strafenArt;
    private int restSekunden;

    @OneToOne
    @JoinColumn(name = "game_event_id")
    private GameEvent ausloesendesEvent;
}
