# Roadmap

## Milestone 0 — Repository and Compatibility

Status: baseline and pinned 1.21.1 dependency source inspection complete. Gradle/CI results are tracked in the active workflow runs.

## Milestone 1 — Technical Foundation

Status: NeoForge entrypoint, Gradle configuration, translations, JUnit infrastructure and CI workflow exist.

## Milestone 2 — MineColonies Integration

Implemented:
- Public API adapter for server-side colony lookup by world position.
- Stable identity keyed by dimension ID + MineColonies colony ID.
- Imperium-owned SavedData in overworld storage for colonies in all dimensions.
- Idempotent reconciliation for existing and new colonies.
- Unit tests for identity and core state observation.
- Required MineColonies dependency family and runtime metadata.

Remaining verification:
- Client smoke CI must pass; earlier client smoke failure was caused by the absence of an OpenAL device in headless CI, not a MineColonies mod-loading exception.
- Persistence has not yet been tested against a real player-created world and save/reload.

## Milestone 3 — Initial Economy and Policies

Implemented on `milestone-3/imperial-economy`:
- Persistent treasury, tax rate, chosen policy, and last collection day.
- Daily-unique tax collection with policy upkeep and non-negative treasury.
- Server-authoritative `/imperium` commands gated by MineColonies Manage Huts permissions.
- Unit tests for economy state, tax uniqueness, upkeep and tax range.
- English and Dutch translations.

Still required:
- Confirm the latest CI run after the command tree fix.
- Test saved-data migration and tax collection in a real colony world.

## Milestone 4 — Parliament and Politics

Implemented on `milestone-4/parliament-politics`:
- Constitutional Empire government display.
- Persisted proposals and enacted legislation per colony.
- One vote per MineColonies colony member and proposal.
- Debate period of one in-game day before resolution.
- Majority vote determines passage; ties and zero-vote proposals fail.
- Three initial laws: Public Works Act, Scholarship Charter and Tax Relief Charter.
- Passing legislation applies an economic effect and records the act.
- English and Dutch translations and pure Java unit tests.

Next implementation:
- Persist emperor identity and appointment/abdication rules.
- Add faction support, approval/happiness modifiers, and peaceful unrest mechanics.
- Replace command-only administration with a proper in-game UI.

## Milestone 5 — Economy and Citizens

- Add named new jobs through complete MineColonies JobEntry, AI, view and building/module support.
- Add a tax collector and philosopher after verifying the full 1.21.1 Job/AI extension contract.
- Link citizen happiness and policy modifiers to MineColonies citizen state without mutating private NBT.

## Milestone 6 — Military

- Add imperial field medic, siege engineer and cavalier only after complete entity/job/AI/guard registration can be verified.
- Keep strategy state server-authoritative; avoid replacing MineColonies combat and guards with abstract-only counters.

## Milestone 7 — Buildings and Visual Progression

- Design one real buildable imperial building and schematic first.
- Add level-based visual/function changes in tested stages.
- Integrate Structurize/BlockUI once the selected APIs are verified.

## Milestone 8 — Empire Simulation and Strategy

- Provincially aggregated economy and domestic development.
- Internal factions, parliament, diplomacy and strategy warfare systems.

## Milestone 9 — Multiplayer and Hardening

- Validate permissions, server/client boundaries, save/load, upgrades and dedicated-server startup.

## Milestone 10 — Release

- Build distributable JAR, verify client/server launch, document dependency versions, setup, translation coverage and migration behavior.
