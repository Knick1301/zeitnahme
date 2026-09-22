package de.student.zeitnahme.controller;

import de.student.zeitnahme.dto.PenaltyTypeDTO;
import de.student.zeitnahme.dto.TeamDTO;
import de.student.zeitnahme.dto.UpdateTeamRequest;
import de.student.zeitnahme.entity.Team;
import de.student.zeitnahme.repository.PenaltyTypeRepository;
import de.student.zeitnahme.repository.TeamRepository;
import org.springframework.web.bind.annotation.*;

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
        return teamRepository.findAll().stream().map(this::toDto).toList();
    }

    @PutMapping("/api/teams/{teamId}")
    public TeamDTO teamAktualisieren(@PathVariable Long teamId, @RequestBody UpdateTeamRequest req) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team nicht gefunden: " + teamId));
        if (req.name() != null && !req.name().isBlank()) {
            team.setName(req.name());
        }
        if (req.logoPfad() != null) {
            team.setLogoPfad(req.logoPfad());
        }
        teamRepository.save(team);
        return toDto(team);
    }

    @GetMapping("/api/strafenarten")
    public List<PenaltyTypeDTO> strafenarten() {
        return penaltyTypeRepository.findAll().stream()
                .map(p -> new PenaltyTypeDTO(p.getId(), p.getName(), p.getDauerSekunden()))
                .toList();
    }

    private TeamDTO toDto(Team t) {
        return new TeamDTO(
                t.getId(),
                t.getName(),
                t.getLogoPfad(),
                t.getSpieler().stream()
                        .map(p -> new TeamDTO.PlayerDTO(p.getId(), p.getName(), p.getNummer()))
                        .toList()
        );
    }
}