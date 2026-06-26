# parkour-gen

Deterministic, infinite, fair **Minecraft parkour course generator** for **26.1.2**.

A course is not a file -- it's a **recipe** (`gen_version + seed + params`). The same
recipe regenerates a **bit-identical course** on every machine: infinite content, perfect
fairness for head-to-head racing, and a tiny payload.

## Status

| Module | What | State |
|---|---|---|
| `parkour-gen-core` | Pure-JVM generator (no Minecraft deps) | ✅ **working, compiles, tested** |
| `parkour-fabric-mod` | Fabric mod: `/parkourgen` builds a course in-world | 🟡 skeleton (pin 26.1.2 mappings) |

**Verified locally:** determinism PASS, and **10,000 generated courses all pass the
constructive solvability verifier (0 failures)**. Golden hash for `seed=12345, tier=5`:
`72cc7d58e6bf69220be51c6043f546bd55a5f89b6c5a7208b9abd95fbeb2fe37`.

## Layout
```
parkour-gen-core/      pure JVM library (the product)
  prng/                deterministic xoroshiro128++ RNG
  physics/             jump table + reach limits  (CALIBRATE on 26.1.2)
  model/               Vec3i, Recipe, GenParams, CourseStructure ...
  generator/           the incremental jump-grammar algorithm
  verify/              constructive solvability verifier
  difficulty/          difficulty scoring + feature vector
  cli/                 GenCli demo / --selftest harness
parkour-fabric-mod/    Fabric mod skeleton + /parkourgen command
docs/                  full build plan
.github/workflows/     CI (compile + self-test)
```

## Build & run the core (no Gradle needed)
```bash
cd parkour-gen-core
find src/main/java -name '*.java' > sources.txt
mkdir -p build/classes && javac -d build/classes @sources.txt

# determinism + 10k-course solvability check
java -cp build/classes dev.codexceed.parkour.cli.GenCli --selftest

# generate a single course
java -cp build/classes dev.codexceed.parkour.cli.GenCli --seed 777 --tier 7 --length 40
```

With Gradle (runs the JUnit suite):
```bash
./gradlew :parkour-gen-core:test
```

## ⚠️ Before production: calibrate physics
The jump table in `physics/JumpTable.java` uses **placeholder constants**. Measure real
sprint-jump/headhitter/neo reach on **Minecraft 26.1.2** and replace them. The generator's
accuracy is only as good as this table. See `docs/parkour_generator_plan.md` §4.

When you change generation output, **bump `GEN_VERSION`** (leaderboards pin to it) and
update the golden hash in `DeterminismTest`.

## Roadmap
See `docs/parkour_generator_plan.md` for the full 7-milestone plan: physics calibration →
core → verifier → Fabric `/parkourgen` MVP → difficulty/themes → backend seed-pool factory →
ladder/matchmaking + UGC.

## License
MIT
