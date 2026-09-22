package de.student.zeitnahme.dto;

import java.util.List;

public record TeamDTO(Long id, String name, String logoPfad, List<String> trainerNamen, List<PlayerDTO> spieler) {
    public record PlayerDTO(Long id, String name, int nummer) {
    }
}