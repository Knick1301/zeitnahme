package de.student.zeitnahme.dto;

import java.util.List;

public record UpdateTeamRequest(String name, String logoPfad, List<String> trainerNamen) {
}