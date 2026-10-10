# Status

Last updated: 2026-10-10 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active development branch: `milestone-2/colony-integration`
- This connected environment uses GitHub file operations and CI; it cannot run a local shell. All build/runtime claims here refer to actual Actions runs.

## Verified implementation
The complete CI workflow passed on commit `489fcf9311883230d1b6b50038ad7560ad59b35b`:
- Gradle build and unit tests: success.
- Dedicated-server start and packaged Structurize schematic validation: success.
- Headless Minecraft client startup: success.
- JAR artifact uploaded.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38062626937

## Major systems
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
- When an imperial tax law is active, 10% of already-collected provincial tax receipts transfer to the central reserve (not a second citizen tax).
- Persistent audit history (max 100 entries per realm), visible via `/imperium empire audit` and the BlockUI ledger.
- Persistent governor appointments for non-capital provinces; Emperor/server operator control, daily stability/legitimacy bonus and unrest reduction.
- English and Dutch translations.
- CI for build/tests, dedicated server/schematic validation and client startup.

## Remaining acceptance gaps
- Real survival test: place both buildings, hire workers, upgrade levels 1–5, and save/reload an existing and a new colony.
- Multiplayer playtest with at least two players for permissions, realm membership, governor appointments and concurrent treasury actions.
- More mechanical/visual differentiation between building levels.
- Strategic logistics: supply routes, multiple fronts and defensive orders; conquest remains secondary to internal development.
- Regional events and broader realm cohesion systems.
- Release notes and a clear distribution/license arrangement.

## Next actions
1. Verify the follow-on governor-effect test and current full CI.
2. Add regional events with deterministic server-side effects and persistent history.
3. Continue save/reload and multiplayer acceptance checks before preparing a release candidate.
