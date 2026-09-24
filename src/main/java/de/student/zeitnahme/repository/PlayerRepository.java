package de.student.zeitnahme.repository;

import de.student.zeitnahme.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamId(Long teamId);

    Optional<Player> findFirstByTeamIdAndNummer(Long teamId, int nummer);
}
