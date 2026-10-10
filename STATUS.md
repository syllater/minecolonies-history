# Status

Last updated: 2026-10-10 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Repository changes are made through GitHub APIs; GitHub Actions handles compile/test/client/server checks.

## Latest fully verified commit
Commit `20a8d8c7b93dce96171e9a7f713dcc33ef607256` passed the complete workflow:
- English/Dutch validator: success for 265 literal and dynamic localization keys with placeholder index/type checks.
- Gradle compile, tests and mod JAR upload: success.
- Headless Minecraft client startup smoke test: success.
- Dedicated server startup and validation of all 10 Structurize schematics: success.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38075571737

The next commit adds a reusable clamped five-tier workforce limit and unit tests. It has not yet been through CI.

## Major systems implemented
- MineColonies API integration with stable identity (dimension + colony ID) and Imperium-owned versioned SavedData.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat MineColonies professions with work AI.
- Imperial Siege Engineer, Field Medic and Cavalier specialist training tracks.
- Archive and guard-tower hiring limits scale with building level, from one worker slot at level 1 to five at level 5.
- Daily taxation, treasury, knowledge investment, economic policies, faction voting and parliamentary decisions.
- Citizen-happiness feedback, faction approval, legitimacy, stability, unrest, strikes and revolts.
- Diplomatic relations and persisted strategic military campaigns.
- Province ranks and agriculture, trade, scholarship, military and civic focus.
- Federated realms with Emperor/capital, up to 64 real colony provinces, invitations and shared central reserve.
- Imperial tax/policy laws, audits, governors, regional events, cohesion and separatist petitions.
- Up to three concurrent operations against distinct targets, persisted doctrine, direct supply routes and temporary emergency defence orders.
- Strategic priority planner and BlockUI controls for doctrine, priority route construction and priority defence.
- `/imperium empire map` renders a coordinate grid from loaded MineColonies colony centers; other-dimension/unresolved colonies are reported separately.
- English/Dutch translation validation for literal and known dynamic keys.
- CI for build/tests, dedicated-server schematic validation and client startup.

## Remaining acceptance gaps
- Real survival test: place both buildings, hire workers, upgrade levels 1–5, then save/reload an existing and a new MineColonies colony.
- Multiplayer playtest with at least two players for permissions, realm membership, governor appointments and concurrent treasury actions.
- More mechanical/visual differentiation between building levels and deeper building modules.
- The coordinate map is a compact text-grid/chat view, not a fully interactive pan/zoom theatre map.
- Release notes, final artifact/package review and an explicit distribution/license decision.

## Next actions
1. Verify the shared hiring-limit helper/tests with a complete CI run.
2. Continue improving high-value runtime/persistence features and defer non-blocking cosmetic warnings.
3. Keep survival and multiplayer tests marked pending until actually performed in Minecraft.
