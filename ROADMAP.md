# Roadmap

## Milestone 0 — Repository and compatibility
**Implementation complete.** Minecraft 1.21.1, Java 21 and NeoForge are pinned; the MineColonies public API/dependency baseline is documented.

## Milestone 1 — Technical foundation
**Implementation complete.** Gradle configuration, EN/NL translations, JUnit infrastructure, pinned dependency declarations and a four-job CI workflow are in place.

## Milestone 2 — MineColonies integration
**Implementation complete and covered by live Minecraft GameTests.** Server-side public API lookup; dimension + colony ID identity; idempotent discovery of existing/new colonies; Imperium-owned versioned SavedData; rename preservation and serialization tests against a real temporary MineColonies colony.

## Milestone 3 — First playable vertical slice
**Automated integration coverage is green; in-world survival acceptance remains open.**
- Imperial Archive and Imperial Guard Tower blocks are linked to real MineColonies building registry entries and the shared colony-building block-entity type.
- Both building families support levels 1–5 and have five Structurize blueprints each (10/10 loaded and validated on a dedicated server).
- A live GameTest places each real hut anchor and checks building registration.
- Philosopher, Tax Collector and Diplomat job registration/assignment and level 1–5 worker capacity are live-tested.
- Siege Engineer, Imperial Field Medic and Cavalier guard registration/assignment and level 1–5 shared guard capacity are live-tested.
- Treasury/taxation, policies, knowledge investment, provincial development and BlockUI ledger are implemented.
- Full Structurize survival construction/upgrade, sustained AI work, visible tier differences and world save/reload still need a human playtest.

## Milestone 4 — Parliament and politics
**Core implementation complete.** Faction voting, tax/policy bills, Emperor assent/veto, shared realm laws, approval/stability/legitimacy/unrest, strikes, revolts and separatist petitions.

## Milestone 5 — Economy and professions
**Core implementation complete and regression-tested.** Daily tax idempotency, policy effects, tier-scaled jobs, province development/focus, atomic audited treasury transfers, imperial tax remittance, treasury conservation and rollback/invalid-input behavior.

## Milestone 6 — Military systems
**Core implementation complete and live-tested at the guard-module boundary.** Specialist training and guard jobs, tier-scaled hiring, three-front campaign cap, postures, supply routes, defensive orders and strategy priorities. Balance and sustained in-world behavior remain unverified.

## Milestone 7 — Buildings and progression
**Automated validation complete; physical survival upgrade acceptance remains open.** Both custom MineColonies hut block-entity registrations, building entries, tier capacity and all 10 Structurize blueprints have passed. CI validates anchors/dimensions; human playtesting must still confirm actual building construction, level upgrades and aesthetics.

## Milestone 8 — Empire and provincial simulation
**Core implementation complete.** Multi-colony realms, capital/Emperor, invitations, central treasury, laws/audit history, governors, events, cohesion/separatist pressure, logistics, dimension-aware strategy commands and graphical Imperial Theatre. Interactive client usability still needs a manual click-through.

## Milestone 9 — Multiplayer and compatibility hardening
**Automated hardening complete; genuine multiplayer acceptance remains open.** Bounded/rate-limited map payloads, server-side permission/treasury checks, EN/NL cross-dimension command contracts, BlockUI ledger button/command-path validation (40 buttons/38 handlers), bounded/malformed SavedData handling, build/test/checksum packaging, real colony/building/job GameTests, client startup and dedicated-server startup with 10/10 schematics are green in [workflow 38285019437](https://github.com/syllater/minecolonies-history/actions/runs/38285019437) on commit `e3e4e7c541ffc0de78daf1a3d8b3483db28d2b22`. The five live GameTests cover tax idempotency/persistence, real colony discovery/SavedData, Archive jobs/capacity, Guard Tower specialists/capacity, and command-level owner versus non-owner tax/policy authorization. A real two-player session is still needed for permission races, concurrent treasury, reconnect and saved-world behavior.

## Milestone 10 — Release
**Self-checking release candidate assembled; final public release is not complete.**
- Candidate version: `0.1.0-rc1`.
- [Download the latest verified release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11763166421).
- [Download the JAR and SHA256SUMS.txt](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762818347).
- Latest JAR SHA-256: `4c4720cadf82f82e944f67134bab8d11ac8d0f74b1056653e274a1868482fbd0`. The added BlockUI validator confirms all 40 ledger buttons are wired to the 38 fixed command handlers or dedicated actions, and all command paths resolve.
- Before final release: complete the survival-world and two-player checks, review gameplay balance and release notes, and make an explicit distribution-license decision. Do not call the project release-ready or publish a final package before these gates are addressed.

## Acceptance principle

Green CI now proves compilation, static checks, artifact integrity, Minecraft client/server startup, MineColonies registry linkage, ten blueprint loads, real colony discovery/persistence, and real building/job/slot-cap assignment tests. It does not claim to have completed the interactive survival and two-player acceptance checklist.


## Latest permission-boundary verification — 2026-10-12

[Workflow 38285019437](https://github.com/syllater/minecolonies-history/actions/runs/38285019437) is green across build/test, client smoke, dedicated server and live GameTests. The fifth GameTest executes real commands through Brigadier and verifies that a non-owner cannot submit tax/policy proposals from another owner's colony or mutate stored proposals. Candidate JAR SHA-256: `4c4720cadf82f82e944f67134bab8d11ac8d0f74b1056653e274a1868482fbd0`.
