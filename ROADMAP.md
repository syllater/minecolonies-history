# Roadmap

## Milestone 0 — Repository and compatibility

Status: baseline created. The MineColonies 1.21.1 source tag and dependency metadata were inspected; dependency resolution has previously passed in CI.

## Milestone 1 — Technical foundation

Status: complete as a development foundation. NeoForge 1.21.1 / Java 21, Gradle wrapper, localization, test setup and GitHub Actions verification exist.

## Milestone 2 — MineColonies integration

Status: implementation and build verified on prior CI runs.
- Public API lookup and discovery for real MineColonies colonies.
- Stable identity (dimension + colony ID).
- Imperium-owned versioned SavedData separate from MineColonies-private NBT.
- Idempotent initialization for existing and new colonies.

## Milestone 3 — First playable vertical slice

Status: core content implemented; in-world playtest remains open.
- Five-tier Imperial Archive and Guard Tower with generated original Structurize blueprints.
- Three citizen professions (Philosopher, Tax Collector, Diplomat) with work AI.
- Specialist military guard roles.
- Treasury, daily taxes, knowledge investment, server-validated commands and BlockUI ledger.
- Persistence for economy, politics, diplomatic relations and military training.
- Previous complete CI baseline verified build/tests, dedicated-server schema validation and client startup. Latest province changes await a green CI run.

## Milestone 4 — Parliament and politics

Status: substantial implementation exists.
- Parliamentary tax/policy bills, faction votes, imperial assent/veto, expiration and audit entries.
- Faction approval, legitimacy, stability, unrest, strikes and revolts based in part on MineColonies happiness.

Remaining: expand decision variety and add in-world UI for proposal resolution.

## Milestone 5 — Economy and citizen professions

Status: substantial implementation exists.
- Economic policies, taxation, treasury investment and knowledge generation.
- Tax Collector and Philosopher roles affect the state; Diplomat generates influence.
- Provincial specialization adds distinct trade, scholarship, civic, military and agricultural effects.
Remaining: more industries/resources and deeper policy/resource coupling.

## Milestone 6 — Military systems

Status: specialist roles, training and time-delayed strategic operations are implemented.
- Imperial Siege Engineer, Field Medic, Cavalier; guard tower hiring modules.
- Three persistent training tracks and army status command.
- Persisted operations: border patrol, relief expedition and war campaign against a real MineColonies colony; costs, duration, training requirements and deterministic outcomes.
- Outcomes change stability, legitimacy, treasury, training and bilateral diplomatic relations without automatically transferring territory.
Remaining: operational theatre/map, supply routes, multiple active fronts, defensive response orders and battle formations. Keep conquest secondary to internal development.

## Milestone 7 — Buildings and progression

Status: two registered custom MineColonies huts support levels 1–5, with ten original Structurize blueprints. Further work should differentiate tiers visually and mechanically and add more buildings.

## Milestone 8 — Empire/provincial simulation

Status: first provincial system now implemented in source:
- Administrative tiers: settlement, county, duchy, principality and kingdom.
- Province focus: agriculture, trade, scholarship, military or civic administration.
- Development consumes 10 knowledge points per action; focus affects economy, stability, unrest, knowledge or military training.
- Status/focus/develop commands and a BlockUI entry point.

Remaining: verify the latest CI and expand provincial governance, governors, regional events and empire-level cohesion.

## Milestone 9 — Multiplayer and compatibility hardening

Status: server-authoritative command mutations and client-side GUI segregation exist; CI includes client and dedicated-server smoke checks. Further acceptance still requires a real multiplayer/in-world save/reload playtest and permission review.

## Milestone 10 — Release

Not complete. Requires green current CI, actual in-world testing of construction and save/reload, changelog/license/distribution decisions, and a release JAR artifact.
