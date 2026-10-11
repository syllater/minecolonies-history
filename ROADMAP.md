# Roadmap

## Milestone 0 — Repository and compatibility
**Implementation complete.** Minecraft 1.21.1, Java 21 and NeoForge baseline are pinned; the public MineColonies API and dependency baseline are documented.

## Milestone 1 — Technical foundation
**Implementation complete.** Gradle wrapper/configuration, English/Dutch translations, unit-test infrastructure, dependency resolution and GitHub Actions verification are in place.

## Milestone 2 — MineColonies integration
**Implementation complete and covered by CI.** Real server-side colony lookup; stable identity by dimension + colony ID; idempotent discovery of old/new colonies; Imperium-owned, versioned SavedData; NBT round-trip and legacy/malformed-record tests.

## Milestone 3 — First playable vertical slice
**Core implementation complete; human survival-world acceptance pending.**
- Imperial Archive and Imperial Guard Tower, each with levels 1–5 and ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat jobs plus specialist guard roles.
- Tier-scaled worker slots/output and guard training, with persistent per-citizen cooldowns.
- Treasury, taxation, policies, knowledge investment, province development and BlockUI ledger.
- Hiring, physical construction/upgrades, worker behavior and real-world save/reload still need a human survival playtest.

## Milestone 4 — Parliament and politics
**Core implementation complete.** Faction voting; tax/policy bills; Emperor assent/veto; shared realm laws; happiness-linked faction approval, stability, legitimacy and unrest; strikes, revolts and separatist petitions. Richer proposal variety remains a possible expansion.

## Milestone 5 — Economy and professions
**Core implementation complete.** Daily taxes, policy effects, knowledge investment, tier-scaled jobs, provincial specializations, diplomatic influence, bounded treasuries and central tax remittance. Provincial ↔ imperial treasury transfers are atomic, audited only on success, and tested for cap/insufficient-funds rollback plus SavedData round-trip. A 100-step alternating-transfer invariant test proves no crowns are created/destroyed, and independent/non-member provinces plus invalid amount/day attempts are rejected without mutation. Daily imperial tax remittance is tested for conservation of already-collected receipts, success-only audit/persistence, and rollback for no-law/invalid-day, insufficient local balance and a full imperial reserve. Deeper coupling to native MineColonies resource-production/industry remains future depth, not a build blocker.

## Milestone 6 — Military systems
**Core implementation complete.** Specialist training, delayed operations, three-front cap, military postures, supply routes, defensive orders and strategic-priority actions. Balance and real multiplayer interaction remain unverified in a survival world.

## Milestone 7 — Buildings and progression
**Automated checks complete; in-world validation pending.** Two registered MineColonies hut families at levels 1–5. CI checks all ten generated schematics, dimensions, anchors and tier-specific architectural detail; dedicated-server startup confirms they load. Actual placement/upgrade aesthetics, hiring and save/reload require a survival test.

## Milestone 8 — Empire and provincial simulation
**Core implementation complete; interactive acceptance pending.** Multi-colony realms, capital/Emperor, invitations, central treasury, laws, audit history, governors, events, cohesion/separatist pressure, route/defense logistics, dimension-aware strategy commands and a graphical Imperial Theatre are implemented. Cross-dimension map projection is restricted to dimensions represented by realm provinces.

## Milestone 9 — Multiplayer and compatibility hardening
**Automated hardening complete; true multiplayer tests pending.** Server-authoritative bounded map payloads, per-player request throttling, treasury/permission regression tests, the seven-command cross-dimension EN/NL contract, malformed SavedData bounds, unique command registration, namespaced dimension parsing, Gradle tests/build, checksum packaging, headless client startup and dedicated-server startup with 10/10 Structurize schematics are green in [workflow 38162651747](https://github.com/syllater/minecolonies-history/actions/runs/38162651747) on commit `d5dc286d2e3e1e1b4ac30b5c7d6af48810fd0952`. Real two-player permission races, treasury concurrency, reconnect and in-world save/load are not covered by headless CI.

## Milestone 10 — Release
**Release candidate assembled; final release is not complete.**
- Version `0.1.0-rc1`.
- A self-checking ZIP contains the mod JAR, its SHA-256 manifest, install/compatibility/release notes and the manual acceptance checklist.
- [Download the latest fully verified RC bundle from workflow 38162651747](https://github.com/syllater/minecolonies-history/actions/runs/38162651747/artifacts/11710624257).
- Before final release: complete the survival-world and two-player playtests, review balance/package/release notes, choose the distribution license and update the final version/license files.

## Current acceptance principle

Automated tests verify compilation, unit logic, client/server startup, registry setup, blueprint loading and artifact integrity. They do not pretend to verify physical hut placement, in-world usability or genuine two-player interaction. Those remain the last meaningful product-acceptance gates.
