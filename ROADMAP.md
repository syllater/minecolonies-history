# Roadmap

## Milestone 0 — Repository and compatibility

Status: baseline and dependency metadata exist. The pinned MineColonies 1.21.1 source tag was inspected for the API used by the integration. Artifact resolution was verified by a CI build on an earlier milestone-2 baseline.

## Milestone 1 — Technical foundation

Status: Java/NeoForge project, Gradle wrapper, dependency configuration, English/Dutch localization, JUnit and a CI build/client-smoke workflow exist. Baseline test evidence: GitHub Actions run 37925143022 passed the build/test job and headless runClient smoke test before the current Milestone 3 source additions.

## Milestone 2 — MineColonies integration

Implemented:
- Server-side public MineColonies API lookup by world position.
- Colony identity keyed by dimension ID plus MineColonies colony ID.
- Imperium-owned overworld SavedData, separate from MineColonies private NBT.
- Idempotent discovery/initialization for existing and new colonies.
- Versioned persistence schema, with older records loading safe defaults.
- Tests for stable identity and safe first-observation behaviour.

The integration baseline has built previously; all changes must keep passing CI as the source evolves.

## Milestone 3 — First playable vertical slice

Source work underway on milestone-2/colony-integration:
- Treasury, tax rate (0–25%), economic policies, stability, knowledge points and idempotent daily tax turns.
- Server-authoritative /imperium commands for status, taxation, policy changes and investments.
- Permission check for economy-changing commands.
- English and Dutch translation keys.
- MineColonies registration for a new Philosopher job, worker AI and Imperial Archive hut/building entry.
- Crafting recipe, blockstate and placeholder block/item model.
- Unit tests for treasury, policy, tax turns, scholarship interval and bounds.

Not yet accepted:
- The new AI failed an early compile because assumed helper methods were missing; a fix now implements those methods locally. The resulting latest commit still needs a fresh CI verdict.
- Structurize level-1 schematic assets for a fully buildable Imperial Archive have not been authored or verified.
- A real save/reload test and in-world worker assignment test remain outstanding.
- A dedicated graphical GUI has not yet been added; this first command interface is the initial management path.

Acceptance:
- Latest test/build and headless runClient jobs pass.
- New job/building registries load without breaking MineColonies.
- Daily taxation is charged at most once per day per colony, and state survives save/reload.
- /imperium command effects are server-side and permission-checked.
- The Philosopher produces knowledge only at the intended interval.
- An Imperial Archive schematic can be built and upgraded in a real MineColonies world.

## Milestone 4 — Parliament and politics

- Government model with Emperor and parliament.
- Council seats, proposal/voting rules and policy-change audit history.
- Stability effects and legitimacy from citizen conditions.

## Milestone 5 — Economy and professions

- Expand treasury, production and maintenance flows.
- Add Diplomat and Tax Collector as full MineColonies professions.
- Translate all gameplay surfaces into English and Dutch.

## Milestone 6 — Military system

- Siege Engineer, Imperial Field Medic and Imperial Cavalier.
- Server-authoritative formation/morale/supply logic.
- Grand-strategy combat with opt-in/clear player control.

## Milestone 7 — Buildings and visual progression

- Structurize schematics with levels 1–5.
- Hybrid visual/function upgrades and upgrade-compatible save migration.
- BlockUI management views where supported by the selected API.

## Milestone 8 — Empire simulation and diplomacy

- Abstract provinces, provincial needs, faction happiness, strikes/revolts and diplomacy.
- Simulation cadence with configurable performance budgets.

## Milestone 9 — Multiplayer hardening

- Server/client authority review, persistence migrations and dedicated-server smoke test.
- Compatibility matrix and actual-world save/reload test.

## Milestone 10 — Release

- Versioned mod JAR, release notes, verified dependencies, translations, known limitations and install instructions.
- Final ./gradlew build and ./gradlew runClient evidence.
