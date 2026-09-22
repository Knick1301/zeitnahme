package de.student.zeitnahme.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fester Strafenkatalog (kleine Strafe, grosse Strafe, ...).
 * Wird einmalig beim Start ueber den DataInitializer befuellt,
 * nicht im laufenden Betrieb über die UI angelegt.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PenaltyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // 0 = keine Zeitstrafe, sondern z.B. Matchstrafe (Spielausschluss)
    @Column(nullable = false)
    private int dauerSekunden;

    public PenaltyType(String name, int dauerSekunden) {
        this.name = name;
        this.dauerSekunden = dauerSekunden;
    }
}
