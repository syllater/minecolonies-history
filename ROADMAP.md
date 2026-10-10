# Roadmap

## Milestone 0 — Repository and compatibility
Status: dependency baseline and MineColonies 1.21.1 public API inspection are in place.

## Milestone 1 — Technical foundation
Status: complete as a development foundation: NeoForge 1.21.1, Java 21, Gradle wrapper, localization, JUnit test setup and CI.

## Milestone 2 — MineColonies integration
Status: implementation and CI compile/test verified. Real colony lookup, identity keyed by dimension + colony ID, idempotent initialization and Imperium-owned SavedData.

## Milestone 3 — First playable vertical slice
Status: substantial gameplay slice implemented:
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize blueprints.
- Philosopher, Tax Collector and Diplomat professions; specialist guard roles.
- Treasury, tax collection, investment, policies, province focus and BlockUI ledger.
- Persistence for politics, diplomacy, training, campaigns, realm membership, audits and governors.
- Needs actual in-world placement/hiring/upgrade/save-reload playtesting.

## Milestone 4 — Parliament and politics
Status: four faction votes, tax/policy bills, imperial assent/veto, common laws, happiness-linked approval/stability/legitimacy/unrest, strikes, revolts and separatist petitions implemented.
Remaining: more bill types and richer in-world proposal controls.

## Milestone 5 — Economy and professions
Status: daily taxation, policy multipliers, investment, worker progression, province specialization and central tax remittance implemented.
Remaining: more industries/resources and deeper resource coupling.

## Milestone 6 — Military systems
Status: specialist roles/training, persistent delayed operations, three-front cap, military postures, direct supply routes and temporary defensive orders implemented.
Added deterministic strategic priority actions for routing and emergency defence.
Remaining: verify long-running strategic balances in a survival world.

## Milestone 7 — Buildings and progression
Status: two registered MineColonies huts with levels 1–5 and ten generated Structurize blueprints.
Remaining: richer level silhouettes/modules and actual construction/upgrade playtesting.

## Milestone 8 — Empire and provincial simulation
Status: realm/capital, invitations, shared treasury, common laws, audited flows, governors, weekly regional events, cohesion and separatist pressure implemented.
A coordinate-based text theatre map from actual MineColonies colony centers is being added.
Remaining: verify the new map and improve realm/event balancing in longer sessions.

## Milestone 9 — Multiplayer and compatibility hardening
Status: server-authoritative commands, permission checks and CI build/server/client checks exist.
Remaining: actual two-player permission/economy session and survival save/reload playtest.

## Milestone 10 — Release
Not complete. Requires current-commit CI, in-world acceptance, release notes, an explicit distribution/license decision and final artifact/package review.
