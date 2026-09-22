package de.student.zeitnahme.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Pfad zu einer lokal abgelegten Datei, keine externe URL (Offline-Faehigkeit!)
    private String logoPfad;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Player> spieler = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }

    public void addSpieler(Player player) {
        spieler.add(player);
        player.setTeam(this);
    }
}
