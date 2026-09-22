package de.student.zeitnahme.config;

import de.student.zeitnahme.entity.PenaltyType;
import de.student.zeitnahme.entity.Player;
import de.student.zeitnahme.entity.Team;
import de.student.zeitnahme.repository.PenaltyTypeRepository;
import de.student.zeitnahme.repository.TeamRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final PenaltyTypeRepository penaltyTypeRepository;

    public DataInitializer(TeamRepository teamRepository, PenaltyTypeRepository penaltyTypeRepository) {
        this.teamRepository = teamRepository;
        this.penaltyTypeRepository = penaltyTypeRepository;
    }

    @Override
    public void run(String... args) {
        if (teamRepository.count() == 0) {
            Team blueArrows = new Team("Blue Arrows Sasbach");
            blueArrows.addSpieler(new Player("Daniel Bühler", 7));
            blueArrows.addSpieler(new Player("Manuel Bauer", 11));
            blueArrows.addSpieler(new Player("Yannick Weber", 23));
            teamRepository.save(blueArrows);

            Team gegner = new Team("Testgegner");
            gegner.addSpieler(new Player("Max Mustermann", 9));
            gegner.addSpieler(new Player("Erika Musterfrau", 14));
            teamRepository.save(gegner);
        }

        if (penaltyTypeRepository.count() == 0) {
            penaltyTypeRepository.saveAll(List.of(
                    new PenaltyType("Kleine Strafe", 120),
                    new PenaltyType("Große Strafe", 300),
                    new PenaltyType("Disziplinarstrafe", 600),
                    new PenaltyType("Matchstrafe", 0)
            ));
        }
    }
}
