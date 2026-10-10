# Status

Last updated: 2026-10-10 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active development branch: `milestone-2/colony-integration`
- This connected environment edits through GitHub APIs and uses Actions for build/runtime checks; it cannot execute a local shell.

## Latest verified commit
Commit `f804506970104b9c0a48f64a04e92e8665e1581e` passed the complete GitHub Actions workflow:
- Gradle compile, unit tests and mod JAR upload: success.
- Headless client startup smoke test: success.
- Dedicated server startup and validation of all 10 generated Structurize schematics: success.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38074390495

The following improvement is now prepared for the next commit: isolate strategic-grid projection into a separately unit-tested helper and add NBT round-trip tests for province economy/profession progress and multi-colony realm persistence. Those new tests are not yet part of a verified commit.

## Major systems implemented
- MineColonies API integration with stable identity (dimension + colony ID) and Imperium-owned versioned SavedData.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat MineColonies professions with work AI.
- Imperial Siege Engineer, Field Medic and Cavalier specialist training tracks.
- Daily taxation, treasury, knowledge investment, economic policies, faction voting and parliamentary decisions.
- Citizen-happiness feedback, faction approval, legitimacy, stability, unrest, strikes and revolts.
- Diplomatic relations and persisted strategic military campaigns.
- Province ranks and agriculture, trade, scholarship, military and civic focus.
- Federated realms with Emperor/capital, up to 64 real colony provinces, invitations and shared central reserve.
- Imperial tax/policy laws voted through the capital propagate to current provinces; new members inherit active laws.
- An enacted imperial tax law transfers 10% of already-collected provincial receipts to the central reserve, not a second citizen tax.
- Persistent audit history, governor appointments, weekly regional events, cohesion and separatist petitions.
- Up to three concurrent operations against distinct targets, persisted military doctrine, direct supply routes and temporary emergency defence orders.
- Strategic priority planner and BlockUI controls for balanced/defensive/offensive doctrine, priority route construction and priority defence.
- `/imperium empire map` plots loaded MineColonies colony centers in the capital dimension and separately lists other-dimension/unresolved colonies.
- English and Dutch translations and permission-checked server commands.
- CI for compile/tests, dedicated-server schematic validation and client startup.

## Remaining acceptance gaps
- Real survival test: place both buildings, hire workers, upgrade levels 1–5, then save/reload existing and new colonies.
- Multiplayer playtest with at least two players for permissions, realm membership, governor appointments and concurrent treasury actions.
- More mechanical/visual differentiation between building levels and deeper building modules.
- The coordinate map is a compact text-grid/chat view, not a fully interactive pan/zoom theatre map.
- Release notes and an explicit distribution/license decision.

## Next actions
1. Add/refine automated save-data round-trip coverage and isolate map projection for tests.
2. Verify those changes with complete CI, then keep moving through the remaining acceptance gaps.
3. Defer non-blocking UI polish while prioritizing correctness, save/reload and multiplayer compatibility.
