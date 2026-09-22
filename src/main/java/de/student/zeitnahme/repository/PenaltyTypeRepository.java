package de.student.zeitnahme.repository;

import de.student.zeitnahme.entity.PenaltyType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaltyTypeRepository extends JpaRepository<PenaltyType, Long> {
}
