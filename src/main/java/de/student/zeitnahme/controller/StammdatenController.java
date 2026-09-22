package de.student.zeitnahme.controller;

import de.student.zeitnahme.dto.PenaltyTypeDTO;
import de.student.zeitnahme.dto.TeamDTO;
import de.student.zeitnahme.repository.PenaltyTypeRepository;
import de.student.zeitnahme.repository.TeamRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class StammdatenController {

    private final TeamRepository teamRepository;
    private final PenaltyTypeRepository penaltyTypeRepository;

    public StammdatenController(TeamRepository teamRepository, PenaltyTypeRepository penaltyTypeRepository) {
        this.teamRepository = teamRepository;
        this.penaltyTypeRepository = penaltyTypeRepository;
    }

    @GetMapping("/api/teams")
    public List<TeamDTO> teams() {
        return teamRepository.findAll().stream()
                .map(t -> new TeamDTO(
                        t.getId(),
                        t.getName(),
                        t.getSpieler().stream()
                                .map(p -> new TeamDTO.PlayerDTO(p.getId(), p.getName(), p.getNummer()))
                                .toList()
                ))
                .toList();
    }

    @GetMapping("/api/strafenarten")
    public List<PenaltyTypeDTO> strafenarten() {
        return penaltyTypeRepository.findAll().stream()
                .map(p -> new PenaltyTypeDTO(p.getId(), p.getName(), p.getDauerSekunden()))
                .toList();
    }
}
