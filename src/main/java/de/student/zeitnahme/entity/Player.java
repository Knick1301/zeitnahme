package de.student.zeitnahme.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int nummer;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    public Player(String name, int nummer) {
        this.name = name;
        this.nummer = nummer;
    }
}
