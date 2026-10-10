# Roadmap

## Milestone 0 — Repository and compatibility
Status: dependency baseline and MineColonies 1.21.1 API inspection completed.

## Milestone 1 — Technical foundation
Status: NeoForge 1.21.1, Java 21, Gradle wrapper, localization, JUnit and CI are in place.

## Milestone 2 — MineColonies integration
Status: implementation and CI compile/test verified. Real colony lookup, identity keyed by dimension + colony ID, idempotent initialization and Imperium-owned SavedData.

## Milestone 3 — First playable vertical slice
Status: substantial gameplay slice implemented:
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize blueprints.
- Philosopher, Tax Collector and Diplomat professions; specialist guard roles.
- Treasury, taxes, investments, policies, province focus and BlockUI ledger.
- Persistent politics, diplomacy, military campaigns, realm membership, audit history and governors.
- NBT round-trip tests verify province economy/profession progress and realm laws/routes/defence/governor/audit data.
- Worker limits scale from one at tier 1 to five at tier 5 and are covered by unit tests.
- Requires in-world placement/hiring/upgrade/save-reload playtesting.

## Milestone 4 — Parliament and politics
Status: faction votes, tax/policy bills, Emperor assent/veto, common laws, happiness-linked approval/stability/legitimacy/unrest, strikes, revolts and separatist petitions implemented.
Remaining: more bill types and richer proposal controls.

## Milestone 5 — Economy and professions
Status: daily taxes, policy multipliers, knowledge investment, worker progression, provincial specializations and central tax remittance implemented.
Remaining: more resource and industry coupling.

## Milestone 6 — Military systems
Status: specialist training, delayed operations, three-front cap, military postures, supply routes, defensive orders and strategic-priority actions implemented.
Remaining: balance checks in a real survival world.

## Milestone 7 — Buildings and progression
Status: two registered MineColonies huts with levels 1–5; ten Structurize schematics load on the dedicated server. Worker slot progression is clamped and unit-tested.
This commit adds a CI check for unique, increasingly sized generated tiers; pending its run.
Remaining: richer tier silhouettes/modules and actual building playtesting.

## Milestone 8 — Empire and provincial simulation
Status: multi-colony realm/capital, invitations, central treasury, laws, audited flows, governors, regional events, cohesion and separatist pressure implemented. Coordinate-based chat map uses colony centers.
Remaining: event balance and map UX.

## Milestone 9 — Multiplayer and compatibility hardening
Status: server-authoritative commands, permission checks, dynamic EN/NL localization validation, NBT round-trip coverage, and full CI build/client/server smoke checks.
Remaining: real two-player and in-world persistence playtests.

## Milestone 10 — Release
Not complete. Requires current CI, in-world acceptance, release notes, final artifact/package review and an explicit distribution/license decision.
