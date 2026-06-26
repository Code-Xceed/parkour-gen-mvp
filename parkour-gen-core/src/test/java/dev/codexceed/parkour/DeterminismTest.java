package dev.codexceed.parkour;

import dev.codexceed.parkour.generator.ParkourGenerator;
import dev.codexceed.parkour.model.GenParams;
import dev.codexceed.parkour.model.Recipe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeterminismTest {
    @Test void sameSeedSameHash() {
        Recipe r = new Recipe(ParkourGenerator.GEN_VERSION, 12345L, GenParams.defaults(5));
        String a = new ParkourGenerator().generate(r).stableHash();
        String b = new ParkourGenerator().generate(
                new Recipe(ParkourGenerator.GEN_VERSION, 12345L, GenParams.defaults(5))).stableHash();
        assertEquals(a, b, "same recipe must produce identical course");
    }

    @Test void goldenHashStable() {
        Recipe r = new Recipe(ParkourGenerator.GEN_VERSION, 12345L, GenParams.defaults(5));
        // Golden anchor: if this changes, gen output changed -> bump GEN_VERSION.
        assertEquals("72cc7d58e6bf69220be51c6043f546bd55a5f89b6c5a7208b9abd95fbeb2fe37",
                new ParkourGenerator().generate(r).stableHash());
    }
}
