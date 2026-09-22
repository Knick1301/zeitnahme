package de.student.zeitnahme.repository;

import de.student.zeitnahme.entity.ActivePenalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivePenaltyRepository extends JpaRepository<ActivePenalty, Long> {
    List<ActivePenalty> findBySpielId(Long spielId);
}
