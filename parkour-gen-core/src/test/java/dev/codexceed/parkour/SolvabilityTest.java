package dev.codexceed.parkour;

import dev.codexceed.parkour.generator.ParkourGenerator;
import dev.codexceed.parkour.model.CourseStructure;
import dev.codexceed.parkour.model.GenParams;
import dev.codexceed.parkour.model.Recipe;
import dev.codexceed.parkour.verify.ConstructiveVerifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SolvabilityTest {
    @Test void allGeneratedCoursesAreSolvable() {
        ParkourGenerator gen = new ParkourGenerator();
        int failures = 0;
        for (int tier = 1; tier <= 10; tier++) {
            for (long s = 0; s < 500; s++) {
                CourseStructure c = gen.generate(new Recipe(
                        ParkourGenerator.GEN_VERSION, s * 131 + tier,
                        new GenParams(tier, 40, 8, 0.3, "quartz")));
                if (!ConstructiveVerifier.verify(c).ok) failures++;
            }
        }
        assertEquals(0, failures, "every generated course must pass the constructive verifier");
    }
}
