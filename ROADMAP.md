# Roadmap

## Milestone 0 — Repository and compatibility
**Implementation and automated verification complete.** Minecraft 1.21.1, Java 21 and NeoForge are pinned; the MineColonies public API/dependency baseline is documented. CI resolves and compiles the selected 1.21.1 artifacts.

## Milestone 1 — Technical foundation
**Complete.** Gradle build, EN/NL localization, JUnit setup, release metadata, reproducible package generation and CI validation are present.

## Milestone 2 — MineColonies integration
**Automated integration complete.** Public API colony lookup, dimension + colony ID identity, idempotent discovery of existing/new colonies, Imperium-owned versioned SavedData, colony-rename preservation and serialization have passed a live test with a real temporary colony.

## Milestone 3 — First playable vertical slice
**Implemented; in-world survival acceptance remains open.**
- Imperial Archive and Imperial Guard Tower are linked to real MineColonies building entries and the shared colony-building block-entity type.
- Both hut families have levels 1–5 and five Structurize schematics each; a dedicated server loads all 10/10.
- Philosopher, Tax Collector and Diplomat job registration/assignment and tier 1–5 capacity are live-tested.
- Siege Engineer, Imperial Field Medic and Cavalier specialist job assignment and tier-scaled shared guard capacity are live-tested.
- Treasury, tax collection, knowledge/progression, policies, Parliament and BlockUI ledger are implemented.
- Physical construction/upgrades, sustained worker AI, visible tier changes and world save/reload still need a survival playtest.

## Milestone 4 — Parliament and politics
**Core implementation and command authorization checks complete.** Faction voting, tax/policy bills, Emperor assent/veto, shared realm laws, stability/legitimacy/unrest, strikes, revolts and separatist petitions are implemented. Live command tests ensure non-owners cannot assent/veto and verify that the Emperor can veto a bill under the Emperor's identity.

## Milestone 5 — Economy and professions
**Core implementation and regression checks complete.** Daily tax idempotency, policy effects, tier-scaled jobs, provincial development/focus, imperial tax remittance, conservation, atomic treasury transfers, insufficient-funds rollback and success-only audit records are tested.

## Milestone 6 — Military systems
**Core implementation complete, with registration/capacity live-tested.** Specialist training, guard jobs, tier capacity, three-front campaign cap, postures, supply routes, defensive orders and strategic priorities are implemented. Balance and sustained in-world behavior remain unverified.

## Milestone 7 — Buildings and progression
**Automated validation complete; survival upgrade acceptance open.** Both custom hut types and all ten schematics pass dedicated-server validation. GameTests check actual MineColonies registration and tier capacity; a human must still confirm physical construction and aesthetics across upgrades.

## Milestone 8 — Empire and provincial simulation
**Core implementation complete.** Multi-colony realms, capital/Emperor, cross-dimension invitations, shared treasury/laws, audit history, governors, regional events, separatist pressure, logistics, and the graphical Imperial Theatre are present. Client interaction remains a manual acceptance task.

## Milestone 9 — Multiplayer and compatibility hardening
**Automated hardening complete; real multiplayer acceptance open.** Bounded map payloads, server-authoritative permission/economy checks, EN/NL command contracts, BlockUI wiring checks, SavedData validation, checksum packaging, client/server startup, real colony/building/job GameTests and cross-dimension invitation GameTest passed in [workflow 38296167316](https://github.com/syllater/minecolonies-history/actions/runs/38296167316). A true two-player session is still needed for races, concurrent treasury, reconnect and actual saved-world behavior.

## Milestone 10 — Release
**Self-checking release candidate assembled; final public release is not complete.**
- Candidate: `0.1.0-rc1`.
- [Release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768472386).
- [Mod JAR + SHA256SUMS.txt](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768327658).
- JAR SHA-256: `351eeafb48e8e6bea538a701c55ae24f10b806df2a3ed15c8979c7701d13bebe`.
- Full CI is green on `a2bf703628c25379dd1c4f4acb1a8aa1e3dc457a`; see [workflow 38296167316](https://github.com/syllater/minecolonies-history/actions/runs/38296167316).
- Before final release: finish survival-world and two-player testing, review balance/release notes and explicitly choose the distribution license. Do not claim final release readiness until these gates are addressed.

## Acceptance principle

CI proves compilation, unit tests, artifact integrity, client/server startup, MineColonies registrations, ten blueprint loads and six live GameTests. It does not claim to have completed the interactive survival and genuine multiplayer acceptance checklist.
