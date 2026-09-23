package de.student.zeitnahme.repository;

import de.student.zeitnahme.entity.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameEventRepository extends JpaRepository<GameEvent, Long> {
    List<GameEvent> findBySpielIdOrderByPeriodeAscSpielzeitSekundenAscIdAsc(Long spielId);
}
