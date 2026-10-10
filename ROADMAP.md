# Roadmap

## Milestone 0 — Repository and compatibility
Status: dependency baseline and MineColonies 1.21.1 public API inspection are in place.

## Milestone 1 — Technical foundation
Status: complete as a development foundation: NeoForge 1.21.1, Java 21, Gradle wrapper, localization, test setup and CI.

## Milestone 2 — MineColonies integration
Status: implementation and CI verified. Real colony lookup, identity keyed by dimension + colony ID, idempotent initialization and Imperium-owned SavedData.

## Milestone 3 — First playable vertical slice
Status: substantial gameplay slice implemented and covered by build/client/server smoke tests.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize blueprints.
- Philosopher, Tax Collector and Diplomat professions; specialist guard roles.
- Treasury, tax collection, investment, policies, province focus and BlockUI ledger.
- Persistence for politics, diplomacy, training, campaigns, realm membership, audit records and governor appointments.
Still requires in-world placement/hiring/upgrade/save-reload playtesting.

## Milestone 4 — Parliament and politics
Status: substantial implementation: four faction votes, tax/policy bills, imperial assent/veto, common laws, happiness-linked approval/stability/legitimacy/unrest, strikes and revolts.
Remaining: more bill types and richer in-world proposal controls.

## Milestone 5 — Economy and professions
Status: daily taxation, policy multipliers, investment, worker progression, province specialization and a 10% remittance of existing provincial receipts under an enacted imperial tax law.
Remaining: more industries/resources and deeper policy/resource coupling.

## Milestone 6 — Military systems
Status: specialist roles/training and persistent delayed strategic operations (border patrol, relief expedition, war campaign) against real MineColonies colonies.
Remaining: theatre map, supply routes, multi-front operations and defensive orders.

## Milestone 7 — Buildings and progression
Status: two registered MineColonies huts with levels 1–5 and ten generated Structurize blueprints. More distinct visuals/modules remain future work.

## Milestone 8 — Empire and provincial simulation
Status: province tiers/focus; federated realm with Emperor/capital, member invitations, common laws, central reserve, audited financial flows and persistent governor appointments.
Governed provinces receive +1 daily stability, +1 legitimacy and reduce unrest by one per processed daily turn.
Remaining: regional events and broader realm cohesion.

## Milestone 9 — Multiplayer and compatibility hardening
Status: server-authoritative mutations and CI smoke tests exist. Still requires a real two-player permission/economy session and a survival save/reload playtest.

## Milestone 10 — Release
Not complete. Requires in-world acceptance, release notes, distribution/license choice and final artifact/package review.
