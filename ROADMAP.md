# Roadmap

## Milestone 0 — Repository and Compatibility

Status: baseline established; the official MineColonies 1.21.1 public API surface and dependency requirements were inspected.

## Milestone 1 — Technical Foundation

Status: NeoForge 1.21.1 baseline, Java 21 toolchain, Gradle wrapper, English/Dutch language files and GitHub Actions build/client-smoke workflow exist.

## Milestone 2 — MineColonies Integration

Status: merged into `main`. Added the colony lookup adapter, stable identity (dimension + colony ID), Imperium-owned schema-versioned SavedData, and idempotent discovery for existing and new colonies. Unit tests and a GitHub Actions `test build` + headless `runClient` smoke run passed on commit `656ba89d35487d56c38e4f6579cb2aae79dcc417`: https://github.com/syllater/minecolonies-history/actions/runs/37925143022

## Milestone 3 — First Playable Vertical Slice

Implementation branch: `milestone-3/vertical-slice`.

Implemented in source; CI verification on the current branch is still required:
- A registered MineColonies Chancery block/building and worker module.
- A registered Diplomat job and AI.
- Per-colony policy, treasury and influence fields in SavedData, with schema v2 migration defaults.
- Balanced, low-tax and emergency-levy policies.
- Tax revenue based on resident counts, gated once per overworld day.
- Emerald deposits into crowns, with server-side permission, inventory and balance checks.
- `/imperium` status, policy and treasury commands.
- Standard MineColonies/BlockUI worker assignment window.
- English/Dutch translations and tests for economic logic.

Known gap: custom level 1–5 Structurize schematics are not yet included, so visual building upgrades are not complete.

## Milestone 4 — Parliament and Government

- Durable constitution/government state and parliament composition.
- Legislative sessions and votes that can pass or reject policies.
- Factions, citizen support, political stability and server-validated BlockUI actions.

## Milestone 5 — Economy and Citizen Services

- Treasury ledger, expenses, wages, operating costs and safe transfers.
- Tax Collector and Philosopher roles with real assignment/work loops.
- Citizen happiness, faction influence, strikes and unrest tied to simulation state.

## Milestone 6 — Military System

- Siege Engineer, Imperial Field Medic and Imperial Cavalier roles.
- Recruitment, supply/upkeep and formation state.
- Strategic-war state machines integrated with real colony/world state.

## Milestone 7 — Building Progression and Visuals

- Valid Structurize schematics for the Chancery and subsequent imperial buildings at levels 1–5.
- Visual upgrade assets that do not copy upstream mod assets.
- Verify build-tool placement, upgrades, repair and save/reload.

## Milestone 8 — Empire Simulation

- Provincial management as an abstract layer over physical MineColonies colonies.
- Diplomacy, trade, stability, events, long-term progression and policy effects.

## Milestone 9 — Multiplayer and Hardening

- Server validation for all commands, packets and GUI mutations.
- Dedicated-server checks, multiple colonies/dimensions and persistence migration.
- Profile periodic systems and reduce heavy tick work.

## Milestone 10 — Release

- Clean build, tests, client startup, release notes, installation/compatibility instructions and JAR artifact.
- Do not mark a milestone verified without actual test evidence.
