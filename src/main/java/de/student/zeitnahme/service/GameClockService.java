package de.student.zeitnahme.service;

import de.student.zeitnahme.dto.SpielStateDTO;
import de.student.zeitnahme.entity.ActivePenalty;
import de.student.zeitnahme.entity.Spiel;
import de.student.zeitnahme.entity.Spielphase;
import de.student.zeitnahme.repository.ActivePenaltyRepository;
import de.student.zeitnahme.repository.SpielRepository;
import de.student.zeitnahme.websocket.GameSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class GameClockService {

    private final SpielRepository spielRepository;
    private final ActivePenaltyRepository activePenaltyRepository;
    private final SpielService spielService;
    private final GameSocketHandler gameSocketHandler;

    public GameClockService(SpielRepository spielRepository,
                            ActivePenaltyRepository activePenaltyRepository,
                            SpielService spielService,
                            GameSocketHandler gameSocketHandler) {
        this.spielRepository = spielRepository;
        this.activePenaltyRepository = activePenaltyRepository;
        this.spielService = spielService;
        this.gameSocketHandler = gameSocketHandler;
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void tick() {
        List<Spiel> laufendeSpiele = spielRepository.findAll().stream()
                .filter(Spiel::isLaeuft)
                .toList();

        for (Spiel spiel : laufendeSpiele) {
            tickEinzelnesSpiel(spiel);
        }

        // Timeouts laufen unabhaengig von der (angehaltenen) Spieluhr ab
        List<Spiel> spieleMitTimeout = spielRepository.findAll().stream()
                .filter(s -> s.getTimeoutTeamId() != null)
                .toList();

        for (Spiel spiel : spieleMitTimeout) {
            tickTimeout(spiel);
        }
    }

    private void tickTimeout(Spiel spiel) {
        int neu = spiel.getTimeoutRestSekunden() - 1;
        if (neu > 0) {
            spiel.setTimeoutRestSekunden(neu);
        } else {
            SpielService.timeoutBeenden(spiel);
        }
        spielRepository.save(spiel);
        gameSocketHandler.broadcast(spiel.getId(), spielService.aktuellerStand(spiel.getId()));
    }

    private void tickEinzelnesSpiel(Spiel spiel) {
        // Strafzeiten laufen nur mit der Spielzeit, nicht in der Pause
        List<ActivePenalty> strafen = spiel.getPhase() == Spielphase.LAUFEND
                ? activePenaltyRepository.findBySpielId(spiel.getId())
                : List.of();
        for (ActivePenalty strafe : strafen) {
            int neu = strafe.getRestSekunden() - 1;
            if (neu <= 0) {
                activePenaltyRepository.delete(strafe);
            } else {
                strafe.setRestSekunden(neu);
                activePenaltyRepository.save(strafe);
            }
        }

        int neueRestzeit = spiel.getRestzeitSekunden() - 1;
        if (neueRestzeit > 0) {
            spiel.setRestzeitSekunden(neueRestzeit);
        } else {
            wechsleZurNaechstenPhase(spiel);
        }
        spielRepository.save(spiel);

        SpielStateDTO dto = spielService.aktuellerStand(spiel.getId());
        gameSocketHandler.broadcast(spiel.getId(), dto);
    }

    private void wechsleZurNaechstenPhase(Spiel spiel) {
        if (spiel.getPhase() == Spielphase.LAUFEND) {
            if (spiel.getPeriode() < spiel.getAnzahlHalbzeiten()) {
                spiel.setPhase(Spielphase.PAUSE);
                spiel.setRestzeitSekunden(spiel.getPausenDauerSekunden());
            } else {
                spiel.setPhase(Spielphase.BEENDET);
                spiel.setLaeuft(false);
                spiel.setRestzeitSekunden(0);
            }
        } else if (spiel.getPhase() == Spielphase.PAUSE) {
            spiel.setPeriode(spiel.getPeriode() + 1);
            spiel.setPhase(Spielphase.LAUFEND);
            SpielService.timeoutsAuffuellen(spiel);
            spiel.setRestzeitSekunden(spiel.getHalbzeitDauerSekunden());
        }
    }
}