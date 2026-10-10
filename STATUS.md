# Status

Last updated: 2026-10-10 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active development branch: `milestone-2/colony-integration`
- This connected environment uses GitHub file operations and CI; it cannot execute a local shell. Build/runtime claims refer only to actual Actions runs.

## Latest verified baseline before the current map commit
Commit `e06f7c71cd8f6e1572df3e35d16996bfa37f1a57` passed the complete GitHub Actions workflow:
- Gradle compile, tests, and JAR upload: success.
- Headless client startup smoke test: success.
- Dedicated server startup and validation of all 10 generated Structurize schematics: success.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38073979260

The current working change adds a coordinate-based strategic map and cross-dimension colony resolution. It still needs its own complete CI run before that feature can be called verified.

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
- Persistent audit history (max 100 entries per realm), visible via `/imperium empire audit` and the BlockUI ledger.
- Persistent governor appointments for non-capital provinces.
- Up to three concurrent operations against distinct targets, persisted military doctrine, direct supply routes and temporary emergency defence orders.
- Strategic priority planner and BlockUI controls for balanced/defensive/offensive doctrine, priority route construction and priority defence.
- English and Dutch translations.
- CI for build/tests, dedicated server/schematic validation and client startup.

## Remaining acceptance gaps
- Real survival test: place both buildings, hire workers, upgrade levels 1–5, then save/reload existing and new colonies.
- Multiplayer playtest with at least two players for permissions, realm membership, governor appointments and concurrent treasury actions.
- More mechanical/visual differentiation between building levels and deeper building modules.
- The new coordinate map is a compact text-grid/chat view, not a fully interactive pan/zoom theatre map; it requires CI verification on the current commit.
- Release notes and an explicit distribution/license decision.

## Next actions
1. Verify the strategic map change with the full current-commit CI workflow.
2. Fix any build/runtime blocking errors; defer non-blocking visual polish.
3. Continue in-world save/reload and multiplayer acceptance gaps, and prepare release documentation only after these limitations are recorded.
