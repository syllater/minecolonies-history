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
- Up to three concurrent operations against distinct targets with persisted balanced/offensive/defensive doctrine.
- Temporary seven-day defensive orders, costing 50 central crowns and adding +20 defensive readiness at resolution.
- Emperor-managed supply routes connect the capital to member provinces, persist condition/upkeep history and add military logistics readiness while operational.
- English and Dutch translations.
- CI for build/tests, dedicated server/schematic validation and client startup.

## Remaining acceptance gaps
- Real survival test: place both buildings, hire workers, upgrade levels 1–5, and save/reload an existing and a new colony.
- Multiplayer playtest with at least two players for permissions, realm membership, governor appointments and concurrent treasury actions.
- More mechanical/visual differentiation between building levels.
- Strategic logistics: direct supply routes, up to three fronts and temporary defensive orders are implemented; a theatre map and in-world strategic planning remain open.
- Regional events and broader realm cohesion systems.
- Release notes and a clear distribution/license arrangement.

## Next actions
1. Verify route/order persistence and campaign bonuses with build/tests/client/server smoke runs.
2. Add a strategic theatre map and in-world strategic planning controls.
3. Continue save/reload and multiplayer acceptance checks before preparing a release candidate.

- The first supply-route CI attempt identified a missing `ImperialSupplyRoute` import in the command handler before tests executed; the follow-up commit fixes it and corrects the route-construction treasury assertion.
