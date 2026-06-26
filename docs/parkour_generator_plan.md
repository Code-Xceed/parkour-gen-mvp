# Parkour Map World Generator — Build Plan
**Target:** Minecraft 26.1.2 (latest) · **Goal:** A deterministic, infinite, fair parkour course generator (`seed + params → identical course`)

---

> ⚠️ **Before writing code — version check:**
> 1. Confirm **Fabric Loader + Fabric API + mappings (Yarn/Mojmap)** and the required **Java version** are published for **26.1.2** (cutting-edge versions can lag by days).
> 2. **All physics constants in this doc must be re-measured on 26.1.2 itself.** Vanilla movement changes between versions — a generator tuned to the wrong version produces impossible or trivial jumps.

---

## 0. The core principle

**A map is not a file. A map is a deterministic recipe:**

```
recipe = { gen_version, params, seed }
```

Same generator version + same seed + same params → **bit-identical course** on every machine.

This single decision gives you:
- ♾️ Infinite content for free (seed space = 2⁶⁴ ≈ 1.8×10¹⁹ per param set)
- ⚖️ Perfect fairness (both racers get the exact same world)
- 📦 Tiny network payload (~200 bytes, not megabytes)
- 🔁 Trivial "no repeat" (track seeds served per player)
- 🛡️ Easy anti-cheat (server regenerates the world and validates runs)

**Golden rule:** keep generation logic in a **pure-JVM library with zero Minecraft dependencies**. The mod and backend verifier both consume that same library — that is what guarantees identical output. *Never put generation logic inside the mod.*

---

## 1. Project structure (3 modules, one shared core)

```
parkour-platform/
├── parkour-gen-core/      # PURE JVM. No Minecraft. The brain. THIS is the product.
│   ├── prng/              # deterministic RNG
│   ├── physics/           # jump table + movement constants
│   ├── generator/         # the course-building algorithm
│   ├── model/             # CourseStructure, BlockPlacement, Recipe
│   ├── difficulty/        # scoring model
│   └── verify/            # constructive solvability checks
├── parkour-fabric-mod/    # Fabric mod. Depends on core. Renders blocks in-world.
└── parkour-verifier/      # Backend worker. Depends on core. Batch validation.
```

The mod is just a **renderer** for the core library. Keep them decoupled.

---

## 2. Tech stack

| Layer | Choice | Why |
|---|---|---|
| Language | **Java 21** (or Kotlin) | MC 1.20.5+ runs Java 21; matches mod toolchain |
| Build | **Gradle 8.x + Fabric Loom** | standard Fabric build system |
| Mod loader | **Fabric Loader + Fabric API** (for 26.1.2) | fastest to support new versions |
| Mappings | **Yarn** or **Mojmap** | readable MC names |
| Core lib | plain JVM module, **no MC deps** | runs identically in mod + backend |
| RNG | **custom xoroshiro128++ / splitmix64** class | deterministic & version-stable |
| Coordinates | **integer grid** only | avoids float nondeterminism |
| Tests | **JUnit 5** + golden-hash tests | lock determinism |
| CI | **GitHub Actions** | run tests + golden checks per commit |
| Recipe format | **JSON** (`{gen_version, seed, params}`) | tiny, signable |
| World type | **void superflat / custom empty chunk generator** | clean canvas; falls = reset |

---

## 3. The generation algorithm (jump-grammar / incremental path)

The course is built **one jump at a time**, each jump drawn from a table of physically-possible moves. Because every move is known-valid by construction, the course is solvable by design — you only verify that moves don't obstruct each other.

```text
function generateCourse(seed, params):
    rng    = DeterministicPRNG(seed)
    course = new CourseStructure()
    cursor = Block(0, 64, 0)              # start pad
    facing = +Z
    course.placeStart(cursor)

    for i in 1 .. params.length:
        move    = pickMove(rng, params.difficulty, facing)   # weighted by tier
        landing = applyPhysics(cursor, move, facing)          # offset from jump table

        if collidesOrBlocksArc(course, cursor, landing, move):
            continue        # reject, redraw (deterministic via rng sequence)

        course.placeBlock(landing, theme.blockFor(move))
        if i % params.checkpointEvery == 0:
            course.placeCheckpoint(landing)

        cursor = landing
        facing = maybeTurn(rng, facing, params.turniness)     # add curves

    course.placeFinish(cursor)
    course.featureVector = computeFeatures(course)
    course.difficulty    = scoreDifficulty(course)
    return course
```

Key behaviors:
- **`pickMove`** — samples the jump table weighted toward the target tier (easy → straight 2–3b jumps; hard → neos, headhitters, precise 4-blocks).
- **`maybeTurn`** — rotates facing to produce curves/spirals, not a boring straight line.
- **Rejection + redraw** — stays deterministic because every draw comes from the same seeded sequence.

---

## 4. Jump table + physics constants (the heart of it)

Each move = `{name, Δ(forward, up, side), requirements, difficulty}`.
**⚠️ Starter values from the parkour community — RE-MEASURE on 26.1.2.**

| Move | Forward gap | Δ height | Notes | Difficulty |
|---|---|---|---|---|
| Walk jump | 2 | 0 | no sprint | 1 |
| Sprint jump (3b) | 3 | 0 | baseline | 2 |
| Sprint jump (4b) | 4 | 0 | max flat, precise | 5 |
| Up-jump +1 | 3 | +1 | shorter reach | 4 |
| Up-jump +2 | 2 | +2 | needs headroom | 6 |
| Down-jump | 4–5 | −1..−3 | drops extend reach | 3 |
| Diagonal | 3 | 0 | side offset | 4 |
| Headhitter | 3 | 0 | block above forces low jump | 7 |
| Neo | 2 | +1 | back-wall bounce | 8 |
| Ladder/vine transfer | — | ±n | climb segment | 3 |
| Momentum jump | 4–5 | 0 | requires running start | 6 |

**Constants to encode in `physics/` (measure on 26.1.2):**
- max flat sprint-jump distance, sprint speed, jump velocity / gravity
- per-elevation distance falloff (each +1 height reduces reach)
- per-drop distance bonus
- player AABB (0.6 × 1.8) for arc / headroom collision checks
- block-type modifiers (ice, slime, soul sand, honey)

**How to measure:** build a calibration map in 26.1.2, perform each jump type at increasing distances, log success/failure, bake the limits into the table. **This calibration IS your generator's accuracy.**

---

## 5. Determinism rules (enforce ruthlessly)

- One **seeded PRNG** threaded through every random decision.
- **Integer math** for all positions — no float ordering bugs.
- **No `HashSet`/`HashMap` iteration** driving generation — use ordered structures.
- **`gen_version` is immutable.** Tuning the table = ship `parkour_v2`. Leaderboards pin to `(mode, gen_version)`.
- **Golden test:** `seed 12345 → SHA-256 of CourseStructure` must never change within a version. CI fails if it does.

---

## 6. Verifier (build alongside the generator)

Because moves come from a valid table, full physics A* isn't needed for v1. Check the **constructive guarantees**:
1. No block placed inside any jump's flight arc (sweep the player AABB through the arc).
2. Headroom present for every jump (2+ blocks clearance).
3. Landing blocks reachable in order, none orphaned.
4. Course inside bounds; checkpoints/start/finish well-formed.

Any failure → reject the seed. *(Later: add a kinematic jump simulator for high-fidelity difficulty grading — but the constructive verifier is enough to ship.)*

---

## 7. Difficulty model

```text
difficulty = w1·avgMoveDifficulty + w2·maxConsecutiveHardJumps
           + w3·precisionJumpCount + w4·turnFrequency + w5·length
```
Bucket into tiers (1–10). **Recalibrate weights from real completion rates after launch.**

---

## 8. World building in the Fabric mod

- World: **void / empty superflat** (custom chunk generator producing air) → any fall = instant reset.
- Command: `/parkourgen <seed> <tier> <length>` → call `parkour-gen-core` → iterate `CourseStructure.placements` → `world.setBlock(pos, state)`.
- **Theme** = block palette (quartz, prismarine, deepslate…) chosen by seed for visual variety.
- **Checkpoints** = marker block + detection region; on fall, teleport to last checkpoint.
- **Start pad** with a `GO` trigger; **finish region** detects completion + records time.

➡️ **This `/parkourgen` command IS your MVP** — it proves the infinite-content engine before any matchmaking/ELO/backend.

---

## 9. Testing

- **Unit:** PRNG determinism, each move's physics math, collision checks.
- **Golden:** fixed seeds → stable structure hashes (regression guard).
- **Property:** generate 10,000 courses across tiers → assert 100% pass the verifier.
- **In-game:** playtest harness that loads N seeds so you can actually run them and validate the physics table feels right.

---

## 10. Build milestones

| # | Milestone | Outcome |
|---|---|---|
| 1 | **Calibrate physics** on 26.1.2 → fill jump table | everything depends on this — do it first |
| 2 | `parkour-gen-core` skeleton: PRNG + model + one move (straight sprint jump) | golden test passes |
| 3 | Full jump table + path algorithm + turns | varied courses generate |
| 4 | Constructive verifier + property test (10k courses) | 100% solvable guarantee |
| 5 | **Fabric mod + void world + `/parkourgen` command** | ✅ **working generator — play it in 26.1.2** |
| 6 | Difficulty model + themes + checkpoints | tiered, polished courses |
| 7 | `parkour-verifier` backend worker → pre-gen validated seed pools per tier | ranked-ready infrastructure |

**Milestone 5 = the working parkour world generator.** Milestones 6–7 turn it into ranked-ready infra.

---

## 11. Data model reference

**Recipe (stored / transmitted):**
```json
{ "gen_version": "parkour_v1", "seed": 8273641923,
  "params": { "tier": 7, "length": 40, "checkpointEvery": 8, "turniness": 0.3, "theme": "ice" } }
```

**CourseStructure (regenerated locally from recipe):**
```text
CourseStructure {
  placements:    [ { pos:(x,y,z), block, role:BLOCK|CHECKPOINT|START|FINISH }, ... ]
  checkpoints:   [ pos, ... ]
  start: pos     finish: pos     bounds: AABB
  featureVector: [ length, jumpHistogram..., turns, avgDifficulty ]
  difficulty:    int (tier 1–10)
}
```

---

## 12. What comes after (the bigger picture)

Once the generator works, layer on:
- **Draw API + per-player seed ledger** → guaranteed no-repeat
- **Map-hash handshake** → both clients prove identical world before "GO"
- **ELO ladder + matchmaking + seasons**
- **Hybrid content:** generated → ranked, curated UGC → casual/featured/tournaments
- **Anti-cheat** (telemetry replay validation), ghosts/replays, daily challenge, battle-royale parkour, tournaments

---

### TL;DR
1. **Measure 26.1.2 physics first.**
2. Build `parkour-gen-core` (pure JVM, deterministic).
3. Verify solvability by construction.
4. Render via a Fabric `/parkourgen` command in a void world.
5. Ship the MVP, then scale to ranked + UGC.
