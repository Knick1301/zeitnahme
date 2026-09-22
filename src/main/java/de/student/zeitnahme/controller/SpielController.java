package de.student.zeitnahme.controller;

import de.student.zeitnahme.dto.*;
import de.student.zeitnahme.service.SpielService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spiele")
@CrossOrigin(origins = "*") // fuers lokale Netz voellig ausreichend, keine oeffentliche API
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
}
