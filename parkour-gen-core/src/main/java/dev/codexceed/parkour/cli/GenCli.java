package dev.codexceed.parkour.cli;

import dev.codexceed.parkour.generator.ParkourGenerator;
import dev.codexceed.parkour.model.*;
import dev.codexceed.parkour.verify.ConstructiveVerifier;

/** Tiny demo/verification CLI. Run with --selftest, or --seed/--tier/--length. */
public final class GenCli {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--selftest")) { selftest(); return; }
        long seed = 12345L; int tier = 5, length = 40;
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--seed":   seed = Long.parseLong(args[i + 1]); break;
                case "--tier":   tier = Integer.parseInt(args[i + 1]); break;
                case "--length": length = Integer.parseInt(args[i + 1]); break;
                default: break;
            }
        }
        Recipe r = new Recipe(ParkourGenerator.GEN_VERSION, seed, new GenParams(tier, length, 8, 0.25, "quartz"));
        CourseStructure c = new ParkourGenerator().generate(r);
        ConstructiveVerifier.Result v = ConstructiveVerifier.verify(c);
        System.out.println("recipe   : " + r.toJson());
        System.out.println("blocks   : " + c.placements.size() + "  checkpoints: " + c.checkpoints.size());
        System.out.println("difficulty: " + c.difficulty);
        System.out.println("start->fin: " + c.start + " -> " + c.finish);
        System.out.println("verify   : " + (v.ok ? "OK" : "FAILED " + v.problems));
        System.out.println("hash     : " + c.stableHash());
    }

    private static void selftest() {
        ParkourGenerator gen = new ParkourGenerator();
        String h1 = gen.generate(new Recipe(ParkourGenerator.GEN_VERSION, 12345L, GenParams.defaults(5))).stableHash();
        String h2 = new ParkourGenerator().generate(new Recipe(ParkourGenerator.GEN_VERSION, 12345L, GenParams.defaults(5))).stableHash();
        boolean det = h1.equals(h2);
        System.out.println("determinism: " + (det ? "PASS" : "FAIL") + "  golden(seed=12345,tier=5)=" + h1);

        int fail = 0, total = 0;
        for (int tier = 1; tier <= 10; tier++) {
            for (long s = 0; s < 1000; s++) {
                total++;
                CourseStructure c = gen.generate(new Recipe(ParkourGenerator.GEN_VERSION, s * 131 + tier, new GenParams(tier, 40, 8, 0.3, "quartz")));
                if (!ConstructiveVerifier.verify(c).ok) fail++;
            }
        }
        System.out.println("property   : " + (fail == 0 ? "PASS" : "FAIL") + "  verified " + total + " courses, failures=" + fail);
        if (!det || fail > 0) System.exit(1);
    }
}
