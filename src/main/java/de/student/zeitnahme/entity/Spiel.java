package de.student.zeitnahme.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Spiel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "heim_team_id", nullable = false)
    private Team heimTeam;

    @ManyToOne
    @JoinColumn(name = "gast_team_id", nullable = false)
    private Team gastTeam;

    private int scoreHeim = 0;
    private int scoreGast = 0;

    private int periode = 1;
    private int anzahlHalbzeiten = 3;
    private int halbzeitDauerSekunden = 20 * 60;
    private int pausenDauerSekunden = 10 * 60;
    private int restzeitSekunden;

    @Enumerated(EnumType.STRING)
    private Spielphase phase = Spielphase.VOR_SPIEL;

    private boolean laeuft = false;

    // Defaults auf DB-Ebene, damit bestehende Spiele beim Schema-Update gueltige Werte bekommen
    @ColumnDefault("1")
    private int timeoutsProPeriode = 1;
    @ColumnDefault("30")
    private int timeoutDauerSekunden = 30;
    @ColumnDefault("1")
    private int verbleibendeTimeoutsHeim = 1;
    @ColumnDefault("1")
    private int verbleibendeTimeoutsGast = 1;

    // Team, das gerade einen Timeout nimmt (null = kein Timeout aktiv)
    private Long timeoutTeamId;
    @ColumnDefault("0")
    private int timeoutRestSekunden = 0;

    private LocalDateTime datum = LocalDateTime.now();

    @OneToMany(mappedBy = "spiel", cascade = CascadeType.ALL)
    private List<GameEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "spiel", cascade = CascadeType.ALL)
    private List<ActivePenalty> laufendeStrafen = new ArrayList<>();

    public Spiel(Team heimTeam, Team gastTeam) {
        this.heimTeam = heimTeam;
        this.gastTeam = gastTeam;
        this.restzeitSekunden = this.halbzeitDauerSekunden;
    }

}
