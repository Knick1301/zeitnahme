package de.student.zeitnahme.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
    private String spielerNameFreitext;
    private Integer spielerNummerFreitext;

    @ManyToOne
    @JoinColumn(name = "assist_player_id")
    private Player assistPlayer;
    private String assistNameFreitext;
    private Integer assistNummerFreitext;

    @ManyToOne
    @JoinColumn(name = "penalty_type_id")
    private PenaltyType penaltyType;

    @ColumnDefault("1")
    private int periode = 1;

    private int spielzeitSekunden;
}