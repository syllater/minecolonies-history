# Roadmap

## Milestone 0 — Repository and compatibility
Status: Minecraft 1.21.1 / Java 21 / NeoForge baseline pinned; public MineColonies API inspected against the 1.21.1 release tag.

## Milestone 1 — Technical foundation
Status: Gradle wrapper, translations, automated tests, dependency resolution and GitHub Actions verification are in place.

## Milestone 2 — MineColonies integration
Status: implemented and included in green build:
- Real colony lookup through MineColonies's public API.
- Stable identity based on dimension + MineColonies colony ID.
- Idempotent discovery for existing and new colonies.
- Imperium-owned, versioned SavedData and NBT round-trip coverage.

## Milestone 3 — First playable vertical slice
Status: substantial vertical slice implemented:
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize blueprints.
- Philosopher, Tax Collector and Diplomat jobs plus specialist guard roles.
- Treasury, taxation, knowledge investments, policies, development and BlockUI ledger.
- Archive tier-scaled Philosopher, Tax Collector and Diplomat output with capped values.
- Guard Tower tier-scaled specialist training on level-up, doubled by Military province focus.
- Persistent save/load for complex province economy/profession state and realm state.
- Worker capacity progression from one slot at tier 1 to five at tier 5, with unit coverage.
- In-world worker hiring, blueprint placement and survival save/reload still need a human playtest.

## Milestone 4 — Parliament and politics
Status: faction voting, tax/policy bills, Emperor assent/veto, shared laws, happiness-linked approval/stability/legitimacy/unrest, strikes, revolts and separatist petitions implemented.
Remaining: more bill types and richer proposal controls.

## Milestone 5 — Economy and professions
Status: daily taxes, policy multipliers, knowledge investment, worker progression and tier-scaled Archive job output, provincial specializations, diplomatic influence and central tax remittance implemented.
Remaining: deeper coupling to MineColonies resource production and industry.

## Milestone 6 — Military systems
Status: specialist training (with Guard Tower tier-based training gain that doubles under Military province focus), delayed operations, three-front cap, military postures, supply routes, defensive orders and strategic-priority actions implemented.
Remaining: balance checks and two-player tests in a real survival world.

## Milestone 7 — Buildings and progression
Status: two registered MineColonies hut families with levels 1–5; all ten Structurize schematics load on the dedicated server. Worker capacity is tier-dependent, and generated schematics now gain oriented eaves, shelving, roof ridge/lantern accents, corner piers, extra windows and tower battlements at defined levels. CI asserts the presence of tier-specific details.
Remaining: inspect each level in a real survival world, test that tier-scaled work output matches the visible hut level, and verify physical placement/upgrades/save-reload.

## Milestone 8 — Empire and provincial simulation
Status: multi-colony realm/capital, invitations, central treasury, laws, audit history, governors, regional events, cohesion/separatist pressure and route/defense logistics implemented. The strategic map has both a chat overview and a graphical Imperial Theatre with server-provided province data, pan/zoom and selection.
Remaining: interactively validate the GUI in a real client and add a clearer view when a realm spans multiple dimensions.

## Milestone 9 — Multiplayer and compatibility hardening
Status: green automated build/unit/client/server-blueprint workflow; custom map payload is bounded, read-only and answered from server state.
Remaining: real two-player permissions, realm invitations/membership races, concurrent treasury operations and reconnect/save-load tests.

## Milestone 10 — Release
Not complete. Requires in-world acceptance, release notes, final artifact/package review and an explicit distribution/license decision.
