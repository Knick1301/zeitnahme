package de.student.zeitnahme.repository;

import de.student.zeitnahme.entity.Spiel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpielRepository extends JpaRepository<Spiel, Long> {
}
