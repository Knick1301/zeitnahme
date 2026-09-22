package de.student.zeitnahme.service;

import de.student.zeitnahme.dto.*;
import de.student.zeitnahme.entity.*;
import de.student.zeitnahme.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpielService {

    private final SpielRepository spielRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final PenaltyTypeRepository penaltyTypeRepository;
    private final GameEventRepository gameEventRepository;
    private final ActivePenaltyRepository activePenaltyRepository;

    public SpielService(SpielRepository spielRepository,
                         TeamRepository teamRepository,
                         PlayerRepository playerRepository,
                         PenaltyTypeRepository penaltyTypeRepository,
                         GameEventRepository gameEventRepository,
                         ActivePenaltyRepository activePenaltyRepository) {
        this.spielRepository = spielRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.penaltyTypeRepository = penaltyTypeRepository;
        this.gameEventRepository = gameEventRepository;
        this.activePenaltyRepository = activePenaltyRepository;
    }

    @Transactional
    public SpielStateDTO spielAnlegen(CreateSpielRequest req) {
        Team heim = teamRepository.findById(req.heimTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Heimteam nicht gefunden: " + req.heimTeamId()));
        Team gast = teamRepository.findById(req.gastTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Gastteam nicht gefunden: " + req.gastTeamId()));

        Spiel spiel = new Spiel(heim, gast);
        spielRepository.save(spiel);
        return toDto(spiel);
    }

    public SpielStateDTO aktuellerStand(Long spielId) {
        return toDto(findSpiel(spielId));
    }

    @Transactional
    public SpielStateDTO torEintragen(Long spielId, GoalRequest req) {
        Spiel spiel = findSpiel(spielId);
        Team team = teamDesSpiels(spiel, req.teamId());

        if (team.getId().equals(spiel.getHeimTeam().getId())) {
            spiel.setScoreHeim(spiel.getScoreHeim() + 1);
        } else {
            spiel.setScoreGast(spiel.getScoreGast() + 1);
        }

        GameEvent event = new GameEvent();
        event.setType(EventType.GOAL);
        event.setSpiel(spiel);
        event.setTeam(team);
        event.setSpielzeitSekunden(verstricheneSekunden(spiel));
        setzeSpieler(event, req.playerId(), req.spielerNameFreitext(), req.spielerNummerFreitext());

        if (req.assistPlayerId() != null) {
            playerRepository.findById(req.assistPlayerId()).ifPresent(event::setAssistPlayer);
        }

        gameEventRepository.save(event);
        spielRepository.save(spiel);
        return toDto(spiel);
    }

    @Transactional
    public SpielStateDTO strafeEintragen(Long spielId, PenaltyRequest req) {
        Spiel spiel = findSpiel(spielId);
        Team team = teamDesSpiels(spiel, req.teamId());
        PenaltyType penaltyType = penaltyTypeRepository.findById(req.penaltyTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Strafenart nicht gefunden: " + req.penaltyTypeId()));

        GameEvent event = new GameEvent();
        event.setType(EventType.PENALTY);
        event.setSpiel(spiel);
        event.setTeam(team);
        event.setPenaltyType(penaltyType);
        event.setSpielzeitSekunden(verstricheneSekunden(spiel));
        setzeSpieler(event, req.playerId(), req.spielerNameFreitext(), req.spielerNummerFreitext());
        gameEventRepository.save(event);

        // Nur bei Zeitstrafen (dauerSekunden > 0) einen laufenden Countdown anlegen -
        // eine Matchstrafe (0 Sekunden) landet nur im Protokoll, blinkt aber nirgends runter.
        if (penaltyType.getDauerSekunden() > 0) {
            ActivePenalty aktiv = new ActivePenalty();
            aktiv.setSpiel(spiel);
            aktiv.setTeam(team);
            aktiv.setPlayer(event.getPlayer());
            aktiv.setSpielerNameFreitext(event.getSpielerNameFreitext());
            aktiv.setSpielerNummerFreitext(event.getSpielerNummerFreitext());
            aktiv.setStrafenArt(penaltyType.getName());
            aktiv.setRestSekunden(penaltyType.getDauerSekunden());
            aktiv.setAusloesendesEvent(event);
            activePenaltyRepository.save(aktiv);
        }

        return toDto(spiel);
    }

    @Transactional
    public SpielStateDTO uhrStart(Long spielId) {
        Spiel spiel = findSpiel(spielId);
        if (spiel.getPhase() == Spielphase.VOR_SPIEL) {
            spiel.setPhase(Spielphase.LAUFEND);
        }
        spiel.setLaeuft(true);
        spielRepository.save(spiel);
        return toDto(spiel);
    }

    @Transactional
    public SpielStateDTO uhrStop(Long spielId) {
        Spiel spiel = findSpiel(spielId);
        spiel.setLaeuft(false);
        spielRepository.save(spiel);
        return toDto(spiel);
    }

    @Transactional
    public SpielStateDTO uhrReset(Long spielId) {
        Spiel spiel = findSpiel(spielId);
        spiel.setLaeuft(false);
        spiel.setRestzeitSekunden(
                spiel.getPhase() == Spielphase.PAUSE
                        ? spiel.getPausenDauerSekunden()
                        : spiel.getHalbzeitDauerSekunden()
        );
        spielRepository.save(spiel);
        return toDto(spiel);
    }

    // --- Hilfsmethoden ---

    private Spiel findSpiel(Long spielId) {
        return spielRepository.findById(spielId)
                .orElseThrow(() -> new IllegalArgumentException("Spiel nicht gefunden: " + spielId));
    }

    private Team teamDesSpiels(Spiel spiel, Long teamId) {
        if (spiel.getHeimTeam().getId().equals(teamId)) {
            return spiel.getHeimTeam();
        }
        if (spiel.getGastTeam().getId().equals(teamId)) {
            return spiel.getGastTeam();
        }
        throw new IllegalArgumentException("Team " + teamId + " gehoert nicht zu Spiel " + spiel.getId());
    }

    private void setzeSpieler(GameEvent event, Long playerId, String nameFreitext, Integer nummerFreitext) {
        if (playerId != null) {
            playerRepository.findById(playerId).ifPresent(event::setPlayer);
        } else {
            event.setSpielerNameFreitext(nameFreitext);
            event.setSpielerNummerFreitext(nummerFreitext);
        }
    }

    // Sehr einfache Naeherung fuer jetzt: verstrichene Zeit der aktuellen
    // Halbzeit. Reicht fuers Protokoll; kann spaeter verfeinert werden
    // (z.B. inkl. vorheriger Halbzeiten), wenn's gebraucht wird.
    private int verstricheneSekunden(Spiel spiel) {
        return spiel.getHalbzeitDauerSekunden() - spiel.getRestzeitSekunden();
    }

    private SpielStateDTO toDto(Spiel spiel) {
        List<SpielStateDTO.PenaltyInfo> strafenHeim = activePenaltyRepository.findBySpielId(spiel.getId()).stream()
                .filter(p -> p.getTeam().getId().equals(spiel.getHeimTeam().getId()))
                .map(this::toPenaltyInfo)
                .collect(Collectors.toList());

        List<SpielStateDTO.PenaltyInfo> strafenGast = activePenaltyRepository.findBySpielId(spiel.getId()).stream()
                .filter(p -> p.getTeam().getId().equals(spiel.getGastTeam().getId()))
                .map(this::toPenaltyInfo)
                .collect(Collectors.toList());

        return new SpielStateDTO(
                spiel.getId(),
                toTeamInfo(spiel.getHeimTeam()),
                toTeamInfo(spiel.getGastTeam()),
                spiel.getScoreHeim(),
                spiel.getScoreGast(),
                spiel.getPeriode(),
                spiel.getAnzahlHalbzeiten(),
                spiel.getPhase(),
                spiel.isLaeuft(),
                spiel.getRestzeitSekunden(),
                strafenHeim,
                strafenGast
        );
    }

    private SpielStateDTO.TeamInfo toTeamInfo(Team team) {
        return new SpielStateDTO.TeamInfo(team.getId(), team.getName(), team.getLogoPfad());
    }

    private SpielStateDTO.PenaltyInfo toPenaltyInfo(ActivePenalty p) {
        String name = p.getPlayer() != null ? p.getPlayer().getName() : p.getSpielerNameFreitext();
        Integer nummer = p.getPlayer() != null ? p.getPlayer().getNummer() : p.getSpielerNummerFreitext();
        return new SpielStateDTO.PenaltyInfo(p.getId(), name, nummer, p.getStrafenArt(), p.getRestSekunden());
    }
}
