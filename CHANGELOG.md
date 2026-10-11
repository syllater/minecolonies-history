# Changelog

This file records user-visible changes. Entries are development notes until a release is explicitly published.

## Unreleased — 0.1.0-rc1 candidate

### Added
- MineColonies-backed provinces with Imperium-owned saved state keyed by dimension and colony ID.
- Imperial Archive and Imperial Guard Tower, each with five generated Structurize tiers.
- Philosopher, Tax Collector and Diplomat jobs, plus Siege Engineer, Field Medic and Cavalier specialist roles.
- Tier-scaled worker/guard capacity, Archive output and specialist military training.
- Treasury, daily tax collection, economic policies, knowledge investments and province development.
- Atomic transfers between provincial treasuries and the imperial reserve, with funds/cap validation and success-only auditing.
- Faction votes, parliamentary tax/policy bills, imperial assent/veto, stability, legitimacy, unrest, strikes, revolts and separatist petitions.
- Diplomacy, federated realms, invitations, a shared imperial treasury, shared laws, audit history, governors and regional events.
- Persistent military operations, postures, supply routes, defensive orders and strategic priority actions.
- BlockUI Imperial Ledger and interactive Imperial Theatre with server-provided province snapshots, pan, zoom, recenter and selection.
- English and Dutch translations.
- Dimension-aware diplomacy, campaign, governor, supply-route, defensive-order and separatist-petition commands resolved against real MineColonies colonies by the server.
- CI contract validation keeps the seven cross-dimension command variants synchronized with their English and Dutch help messages.
- Automated tests for state persistence, policy/economy bounds, realm membership, strategic map payloads, work cooldowns and tier progression.
- Release-candidate metadata validation for the Minecraft/NeoForge line, required dependency declarations, version format and mandatory manual release gates.
- Cross-dimension supply-route building, defensive orders and governor appointments/dismissals for realm-member provinces.
- Imperial Theatre dimension selector; clients may request a map projection only for a dimension that actually contains a province in their server-authoritative realm.
- Per-player server-side cooldown for map snapshot requests, preventing request spam from repeatedly rebuilding colony snapshots.
- CI validation for translations and generated blueprints, Gradle build/tests, client startup and dedicated-server schematic loading.
- CI runs only once for feature-branch pull requests, avoiding duplicate Minecraft smoke-test jobs per branch commit.
- Downloadable CI artifacts include a SHA-256 manifest for verifying the built mod JAR.

### Fixed / hardened
- Server-authoritative map snapshot construction; client requests cannot select arbitrary empire/province identifiers.
- Bounded map payload fields/province count and bounded per-citizen work cooldown history.
- Persisted work cooldowns prevent duplicate profession output immediately after a save/reload.
- Imperial treasury transfers validate both balances before mutation; failed cap/insufficient-funds cases leave both balances and audit history unchanged.
- Regression tests verify that 100 mixed transfers conserve total crowns, invalid amounts/day indexes do not mutate balances, and independent provinces cannot exchange funds with a realm treasury.
- Daily imperial tax remittance is tested for conservation of existing receipts, success-only audit and persistence, missing-law/invalid-day rejection, insufficient provincial funds and full-reserve rollback.
- Dedicated-server class loading is isolated from client-only map UI code.

### Known acceptance gaps
- Physical hut construction, worker hiring and levels 1–5 need a survival-world playtest.
- The graphical theatre needs a real in-client click-through test.
- Two-player realm permissions, treasury concurrency, reconnect and multiplayer save/reload need dedicated-server testing.
- Gameplay balance, final package review and the distribution/license decision remain open.
