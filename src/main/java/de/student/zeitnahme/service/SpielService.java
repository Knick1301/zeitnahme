package de.student.zeitnahme.service;

import de.student.zeitnahme.dto.*;
import de.student.zeitnahme.entity.*;
import de.student.zeitnahme.repository.*;
import de.student.zeitnahme.websocket.GameSocketHandler;
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
    private final GameSocketHandler gameSocketHandler;

    public SpielService(SpielRepository spielRepository,
                        TeamRepository teamRepository,
                        PlayerRepository playerRepository,
                        PenaltyTypeRepository penaltyTypeRepository,
                        GameEventRepository gameEventRepository,
                        ActivePenaltyRepository activePenaltyRepository,
                        GameSocketHandler gameSocketHandler) {
        this.spielRepository = spielRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.penaltyTypeRepository = penaltyTypeRepository;
        this.gameEventRepository = gameEventRepository;
        this.activePenaltyRepository = activePenaltyRepository;
        this.gameSocketHandler = gameSocketHandler;
    }

    @Transactional
    public SpielStateDTO spielAnlegen(CreateSpielRequest req) {
        Team heim = teamRepository.findById(req.heimTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Heimteam nicht gefunden: " + req.heimTeamId()));
        Team gast = teamRepository.findById(req.gastTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Gastteam nicht gefunden: " + req.gastTeamId()));

        Spiel spiel = new Spiel(heim, gast);
        if (req.anzahlHalbzeiten() != null) {
            spiel.setAnzahlHalbzeiten(req.anzahlHalbzeiten());
        }
        if (req.halbzeitDauerSekunden() != null) {
            spiel.setHalbzeitDauerSekunden(req.halbzeitDauerSekunden());
            spiel.setRestzeitSekunden(req.halbzeitDauerSekunden());
        }
        if (req.timeoutsProPeriode() != null) {
            spiel.setTimeoutsProPeriode(req.timeoutsProPeriode());
            spiel.setVerbleibendeTimeoutsHeim(req.timeoutsProPeriode());
            spiel.setVerbleibendeTimeoutsGast(req.timeoutsProPeriode());
        }
        if (req.timeoutDauerSekunden() != null) {
            spiel.setTimeoutDauerSekunden(req.timeoutDauerSekunden());
        }
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
        event.setPeriode(spiel.getPeriode());
        event.setSpielzeitSekunden(verstricheneSekunden(spiel));
        setzeSpieler(event, req.playerId(), req.spielerNameFreitext(), req.spielerNummerFreitext());

        if (req.assistPlayerId() != null) {
            playerRepository.findById(req.assistPlayerId()).ifPresent(event::setAssistPlayer);
        } else if ((req.assistNameFreitext() != null && !req.assistNameFreitext().isBlank())
                || req.assistNummerFreitext() != null) {
            event.setAssistNameFreitext(req.assistNameFreitext());
            event.setAssistNummerFreitext(req.assistNummerFreitext());
        }

        gameEventRepository.save(event);
        spielRepository.save(spiel);
        return toDtoUndBroadcasten(spiel);
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
        event.setPeriode(spiel.getPeriode());
        event.setSpielzeitSekunden(verstricheneSekunden(spiel));
        setzeSpieler(event, req.playerId(), req.spielerNameFreitext(), req.spielerNummerFreitext());
        gameEventRepository.save(event);

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

        return toDtoUndBroadcasten(spiel);
    }

    @Transactional
    public SpielStateDTO uhrStart(Long spielId) {
        Spiel spiel = findSpiel(spielId);
        if (spiel.getPhase() == Spielphase.BEENDET) {
            throw new IllegalStateException("Das Spiel ist beendet");
        }
        if (spiel.getPhase() == Spielphase.VOR_SPIEL) {
            spiel.setPhase(Spielphase.LAUFEND);
        }
        // Wer die Uhr wieder startet, beendet einen laufenden Timeout vorzeitig
        timeoutBeenden(spiel);
        spiel.setLaeuft(true);
        spielRepository.save(spiel);
        return toDtoUndBroadcasten(spiel);
    }

    @Transactional
    public SpielStateDTO uhrStop(Long spielId) {
        Spiel spiel = findSpiel(spielId);
        spiel.setLaeuft(false);
        spielRepository.save(spiel);
        return toDtoUndBroadcasten(spiel);
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
        return toDtoUndBroadcasten(spiel);
    }

    @Transactional
    public SpielStateDTO uhrSetzen(Long spielId, int sekunden) {
        Spiel spiel = findSpiel(spielId);
        spiel.setRestzeitSekunden(Math.max(0, sekunden));
        spielRepository.save(spiel);
        return toDtoUndBroadcasten(spiel);
    }

    @Transactional
    public SpielStateDTO pauseStarten(Long spielId, int dauerSekunden) {
        Spiel spiel = findSpiel(spielId);
        spiel.setPhase(Spielphase.PAUSE);
        spiel.setRestzeitSekunden(dauerSekunden);
        spiel.setLaeuft(true);
        spielRepository.save(spiel);
        return toDtoUndBroadcasten(spiel);
    }

    @Transactional
    public SpielStateDTO timeoutNehmen(Long spielId, Long teamId) {
        Spiel spiel = findSpiel(spielId);
        Team team = teamDesSpiels(spiel, teamId);
        if (spiel.isLaeuft()) {
            throw new IllegalStateException("Timeout nur bei gestoppter Uhr moeglich");
        }
        if (spiel.getTimeoutTeamId() != null) {
            throw new IllegalStateException("Es laeuft bereits ein Timeout");
        }
        boolean heim = team.getId().equals(spiel.getHeimTeam().getId());
        int verbleibend = heim ? spiel.getVerbleibendeTimeoutsHeim() : spiel.getVerbleibendeTimeoutsGast();
        if (verbleibend <= 0) {
            throw new IllegalStateException("Keine Timeouts mehr fuer " + team.getName());
        }
        if (heim) {
            spiel.setVerbleibendeTimeoutsHeim(verbleibend - 1);
        } else {
            spiel.setVerbleibendeTimeoutsGast(verbleibend - 1);
        }
        spiel.setTimeoutTeamId(team.getId());
        spiel.setTimeoutRestSekunden(spiel.getTimeoutDauerSekunden());
        spielRepository.save(spiel);
        return toDtoUndBroadcasten(spiel);
    }

    @Transactional
    public SpielStateDTO timeoutsZuruecksetzen(Long spielId) {
        Spiel spiel = findSpiel(spielId);
        timeoutsAuffuellen(spiel);
        spielRepository.save(spiel);
        return toDtoUndBroadcasten(spiel);
    }

    public static void timeoutsAuffuellen(Spiel spiel) {
        spiel.setVerbleibendeTimeoutsHeim(spiel.getTimeoutsProPeriode());
        spiel.setVerbleibendeTimeoutsGast(spiel.getTimeoutsProPeriode());
    }

    public static void timeoutBeenden(Spiel spiel) {
        spiel.setTimeoutTeamId(null);
        spiel.setTimeoutRestSekunden(0);
    }

    public List<GameEventDTO> protokoll(Long spielId) {
        return gameEventRepository.findBySpielIdOrderByPeriodeAscSpielzeitSekundenAscIdAsc(spielId).stream()
                .map(this::toEventDto)
                .toList();
    }

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

    private int verstricheneSekunden(Spiel spiel) {
        // In der Pause zeigt die Uhr die Pausenzeit - Ereignisse zaehlen zum Ende der abgelaufenen Periode
        if (spiel.getPhase() == Spielphase.PAUSE) {
            return spiel.getHalbzeitDauerSekunden();
        }
        return Math.max(0, spiel.getHalbzeitDauerSekunden() - spiel.getRestzeitSekunden());
    }

    private SpielStateDTO toDtoUndBroadcasten(Spiel spiel) {
        SpielStateDTO dto = toDto(spiel);
        gameSocketHandler.broadcast(spiel.getId(), dto);
        return dto;
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
                strafenGast,
                spiel.getTimeoutsProPeriode(),
                spiel.getTimeoutDauerSekunden(),
                spiel.getVerbleibendeTimeoutsHeim(),
                spiel.getVerbleibendeTimeoutsGast(),
                spiel.getTimeoutTeamId(),
                spiel.getTimeoutRestSekunden()
        );
    }

    private SpielStateDTO.TeamInfo toTeamInfo(Team team) {
        return new SpielStateDTO.TeamInfo(team.getId(), team.getName(), team.getLogoPfad());
    }

    private SpielStateDTO.PenaltyInfo toPenaltyInfo(ActivePenalty p) {
        String name = p.getPlayer() != null ? p.getPlayer().getName() : p.getSpielerNameFreitext();
        Integer nummer = p.getPlayer() != null ? Integer.valueOf(p.getPlayer().getNummer()) : p.getSpielerNummerFreitext();
        return new SpielStateDTO.PenaltyInfo(p.getId(), name, nummer, p.getStrafenArt(), p.getRestSekunden());
    }

    private GameEventDTO toEventDto(GameEvent e) {
        String spielerName = e.getPlayer() != null ? e.getPlayer().getName() : e.getSpielerNameFreitext();
        Integer spielerNummer = e.getPlayer() != null ? Integer.valueOf(e.getPlayer().getNummer()) : e.getSpielerNummerFreitext();
        String assistName = e.getAssistPlayer() != null ? e.getAssistPlayer().getName() : e.getAssistNameFreitext();
        Integer assistNummer = e.getAssistPlayer() != null ? Integer.valueOf(e.getAssistPlayer().getNummer()) : e.getAssistNummerFreitext();
        return new GameEventDTO(
                e.getType().name(),
                e.getTeam().getId(),
                e.getTeam().getName(),
                spielerName,
                spielerNummer,
                assistName,
                assistNummer,
                e.getPenaltyType() != null ? e.getPenaltyType().getName() : null,
                e.getPenaltyType() != null ? Integer.valueOf(e.getPenaltyType().getDauerSekunden()) : null,
                e.getPeriode(),
                e.getSpielzeitSekunden()
        );
    }
}