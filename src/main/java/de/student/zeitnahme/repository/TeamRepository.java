package de.student.zeitnahme.repository;

import de.student.zeitnahme.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
