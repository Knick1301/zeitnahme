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

    private String logoPfad;

    @ElementCollection
    @CollectionTable(name = "team_trainer", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "name")
    private List<String> trainerNamen = new ArrayList<>();

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