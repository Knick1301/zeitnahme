package de.student.zeitnahme.controller;

import de.student.zeitnahme.dto.*;
import de.student.zeitnahme.service.SpielService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spiele")
@CrossOrigin(origins = "*")
public class SpielController {

    private final SpielService spielService;

    public SpielController(SpielService spielService) {
        this.spielService = spielService;
    }

    @PostMapping
    public SpielStateDTO spielAnlegen(@RequestBody CreateSpielRequest request) {
        return spielService.spielAnlegen(request);
    }

    @GetMapping("/{spielId}")
    public SpielStateDTO aktuellerStand(@PathVariable Long spielId) {
        return spielService.aktuellerStand(spielId);
    }

    @PostMapping("/{spielId}/tor")
    public SpielStateDTO torEintragen(@PathVariable Long spielId, @RequestBody GoalRequest request) {
        return spielService.torEintragen(spielId, request);
    }

    @PostMapping("/{spielId}/strafe")
    public SpielStateDTO strafeEintragen(@PathVariable Long spielId, @RequestBody PenaltyRequest request) {
        return spielService.strafeEintragen(spielId, request);
    }

    @PostMapping("/{spielId}/uhr/start")
    public SpielStateDTO uhrStart(@PathVariable Long spielId) {
        return spielService.uhrStart(spielId);
    }

    @PostMapping("/{spielId}/uhr/stop")
    public SpielStateDTO uhrStop(@PathVariable Long spielId) {
        return spielService.uhrStop(spielId);
    }

    @PostMapping("/{spielId}/uhr/reset")
    public SpielStateDTO uhrReset(@PathVariable Long spielId) {
        return spielService.uhrReset(spielId);
    }

    @PutMapping("/{spielId}/uhr/setzen")
    public SpielStateDTO uhrSetzen(@PathVariable Long spielId, @RequestBody SetZeitRequest request) {
        return spielService.uhrSetzen(spielId, request.sekunden());
    }

    @PostMapping("/{spielId}/uhr/pause")
    public SpielStateDTO pauseStarten(@PathVariable Long spielId, @RequestBody PauseRequest request) {
        return spielService.pauseStarten(spielId, request.dauerSekunden());
    }

    @PostMapping("/{spielId}/timeout")
    public SpielStateDTO timeoutNehmen(@PathVariable Long spielId, @RequestBody TimeoutRequest request) {
        return spielService.timeoutNehmen(spielId, request.teamId());
    }

    @PostMapping("/{spielId}/timeouts/zuruecksetzen")
    public SpielStateDTO timeoutsZuruecksetzen(@PathVariable Long spielId) {
        return spielService.timeoutsZuruecksetzen(spielId);
    }

    @GetMapping("/{spielId}/protokoll")
    public List<GameEventDTO> protokoll(@PathVariable Long spielId) {
        return spielService.protokoll(spielId);
    }
}